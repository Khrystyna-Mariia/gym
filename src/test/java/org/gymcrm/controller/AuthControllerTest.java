package org.gymcrm.controller;

import org.gymcrm.actuator.GymMetrics;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.model.Role;
import org.gymcrm.model.User;
import org.gymcrm.security.JwtService;
import org.gymcrm.security.LoginAttemptService;
import org.gymcrm.security.TokenBlacklistService;
import org.gymcrm.security.UserPrincipal;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private UserPrincipal traineePrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("hashed");
        user.setActive(true);
        user.setRole(Role.TRAINEE);
        traineePrincipal = new UserPrincipal(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_returnsOkAndToken_whenCredentialsValid() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(traineePrincipal);
        when(jwtService.generateToken(traineePrincipal)).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"pass123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(loginAttemptService).loginSucceeded("john.doe");
        verify(gymMetrics).incrementLoginSuccess();
        verify(gymMetrics, never()).incrementLoginFailure();
    }

    @Test
    void login_returnsUnauthorized_whenCredentialsInvalid() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).loginFailed("john.doe");
        verify(gymMetrics).incrementLoginFailure();
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_returnsLocked_whenUserIsBlocked() throws Exception {
        when(loginAttemptService.isBlocked("john.doe")).thenReturn(true);
        when(loginAttemptService.remainingBlockDuration("john.doe")).thenReturn(Duration.ofMinutes(3));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john.doe","password":"anything"}
                                """))
                .andExpect(status().is(423));

        verifyNoInteractions(authenticationManager);
        verify(loginAttemptService, never()).loginFailed(any());
    }

    @Test
    void logout_blacklistsToken_whenBearerHeaderPresent() throws Exception {
        Instant expiry = Instant.parse("2026-06-01T00:00:00Z");
        when(jwtService.extractExpiration("valid-token")).thenReturn(expiry);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-token")
                        .with(user(traineePrincipal)))
                .andExpect(status().isOk());

        verify(tokenBlacklistService).blacklist("valid-token", expiry);
    }

    @Test
    void logout_doesNothing_whenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(user(traineePrincipal)))
                .andExpect(status().isOk());

        verifyNoInteractions(tokenBlacklistService, jwtService);
    }

    @Test
    void changePassword_returnsOk_andDelegatesToTraineeService() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(traineePrincipal, null, traineePrincipal.getAuthorities())
        );

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"old123","newPassword":"new456"}
                                """))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("john.doe", "old123", "new456");
        verifyNoInteractions(trainerService);
    }

    @Test
    void changePassword_delegatesToTrainerService_whenRoleIsTrainer() throws Exception {
        User trainerUser = new User();
        trainerUser.setUsername("anna.k");
        trainerUser.setPassword("hashed");
        trainerUser.setActive(true);
        trainerUser.setRole(Role.TRAINER);
        UserPrincipal trainerPrincipal = new UserPrincipal(trainerUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(trainerPrincipal, null, trainerPrincipal.getAuthorities())
        );

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"old123","newPassword":"new456"}
                                """))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("anna.k", "old123", "new456");
        verifyNoInteractions(traineeService);
    }
}