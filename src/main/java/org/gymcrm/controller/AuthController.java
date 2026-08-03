package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.gymcrm.actuator.GymMetrics;
import org.gymcrm.dto.request.ChangeLoginRequest;
import org.gymcrm.dto.request.LoginRequest;
import org.gymcrm.dto.response.LoginResponse;
import org.gymcrm.exception.AccountLockedException;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.model.Role;
import org.gymcrm.security.JwtService;
import org.gymcrm.security.LoginAttemptService;
import org.gymcrm.security.TokenBlacklistService;
import org.gymcrm.security.UserPrincipal;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, logout and password management")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final GymMetrics gymMetrics;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          TraineeService traineeService,
                          TrainerService trainerService,
                          GymMetrics gymMetrics,
                          LoginAttemptService loginAttemptService,
                          TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.gymMetrics = gymMetrics;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT bearer token", description = "Public endpoint")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String username = request.username();

        if (loginAttemptService.isBlocked(username)) {
            long minutesLeft = loginAttemptService.remainingBlockDuration(username).toMinutes() + 1;
            logger.warn("Login attempt rejected for blocked user: {}", username);
            throw new AccountLockedException(
                    "Account temporarily locked due to too many failed login attempts. Try again in "
                            + minutesLeft + " minute(s).");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            loginAttemptService.loginSucceeded(username);
            gymMetrics.incrementLoginSuccess();
            logger.info("Successful login for user: {}", username);
            return new LoginResponse(token);
        } catch (org.springframework.security.core.AuthenticationException e) {
            loginAttemptService.loginFailed(username);
            gymMetrics.incrementLoginFailure();
            logger.warn("Login failed for user: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current JWT", description = "Requires a valid Bearer token; blacklists it so it can no longer be used")
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        java.time.Instant expiresAt = jwtService.extractExpiration(token);
        tokenBlacklistService.blacklist(token, expiresAt);
        logger.info("User logged out, token blacklisted");
    }

    @PutMapping("/password")
    @Operation(summary = "Change password for the currently authenticated account")
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangeLoginRequest request) {
        String username = principal.getUsername();

        if (principal.user().getRole() == Role.TRAINEE) {
            traineeService.changePassword(username, request.oldPassword(), request.newPassword());
        } else {
            trainerService.changePassword(username, request.oldPassword(), request.newPassword());
        }
    }
}