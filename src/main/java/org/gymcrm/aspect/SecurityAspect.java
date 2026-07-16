package org.gymcrm.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.gymcrm.context.UserContextHolder;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAspect.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public SecurityAspect(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Before("@annotation(org.gymcrm.annotation.RequireAuth)")
    public void authenticate() {
        var auth = UserContextHolder.getAuth();
        if (auth != null && auth.isAuthenticated()) {
            logger.debug("User '{}' authenticated in-memory with role '{}'", auth.username(), auth.role());
            return;
        }

        var creds = UserContextHolder.getCredentials();
        if (creds == null || creds.username() == null || creds.password() == null) {
            logger.warn("Authentication failed: no authenticated user or credentials in context");
            throw new AuthenticationException("Authentication failed: credentials missing");
        }

        String username = creds.username();
        String password = creds.password();

        boolean isTrainee = traineeService.authenticate(username, password);
        String role = "TRAINEE";

        if (!isTrainee) {
            boolean isTrainer = trainerService.authenticate(username, password);
            if (!isTrainer) {
                logger.warn("Authentication failed for user: {}", username);
                throw new AuthenticationException("Authentication failed: invalid username or password");
            }
            role = "TRAINER";
        }

        UserContextHolder.setAuthenticated(username, role);
        logger.debug("User '{}' single-checked via DB and promoted to in-memory context as {}", username, role);
    }
}