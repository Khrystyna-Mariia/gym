package org.gymcrm.aspect;

import org.gymcrm.context.UserContextHolder;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAspectTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private SecurityAspect securityAspect;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        UserContextHolder.clear();
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreMissingInContext() {
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> securityAspect.authenticate());

        assertEquals("Authentication failed: credentials missing", exception.getMessage());
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        UserContextHolder.setCredentials(null, "password123");

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> securityAspect.authenticate());

        assertEquals("Authentication failed: credentials missing", exception.getMessage());
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        UserContextHolder.setCredentials("John.Smith", null);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> securityAspect.authenticate());

        assertEquals("Authentication failed: credentials missing", exception.getMessage());
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFailsForBothRoles() {
        String username = "wrong.user";
        String password = "wrong.password";
        UserContextHolder.setCredentials(username, password);

        when(traineeService.authenticate(username, password)).thenReturn(false);
        when(trainerService.authenticate(username, password)).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> securityAspect.authenticate());

        assertEquals("Authentication failed: invalid username or password", exception.getMessage());
        verify(traineeService).authenticate(username, password);
        verify(trainerService).authenticate(username, password);
    }

    @Test
    void shouldNotThrowExceptionWhenTraineeAuthenticatesSuccessfully() {
        String username = "john.trainee";
        String password = "password123";
        UserContextHolder.setCredentials(username, password);

        when(traineeService.authenticate(username, password)).thenReturn(true);

        assertDoesNotThrow(() -> securityAspect.authenticate());
    }

    @Test
    void shouldNotThrowExceptionWhenTrainerAuthenticatesSuccessfully() {
        String username = "alex.trainer";
        String password = "password123";
        UserContextHolder.setCredentials(username, password);

        when(traineeService.authenticate(username, password)).thenReturn(false);
        when(trainerService.authenticate(username, password)).thenReturn(true);

        assertDoesNotThrow(() -> securityAspect.authenticate());
    }
}