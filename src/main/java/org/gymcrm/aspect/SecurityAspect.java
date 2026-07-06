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
        UserContextHolder.UserCredentials credentials = UserContextHolder.getCredentials();

        if (credentials == null || credentials.username() == null || credentials.password() == null) {
            logger.warn("Authentication failed: No credentials provided in UserContextHolder");
            throw new AuthenticationException("Authentication failed: credentials missing");
        }

        String username = credentials.username();
        String password = credentials.password();

        boolean isTrainee = traineeService.authenticate(username, password);
        boolean isTrainer = trainerService.authenticate(username, password);

        if (!isTrainee && !isTrainer) {
            logger.warn("Authentication failed for user: {}", username);
            throw new AuthenticationException("Authentication failed: invalid username or password");
        }

        logger.debug("User '{}' successfully authenticated via AOP", username);
    }
}