package org.gymcrm;

import org.gymcrm.config.AppConfig;
import org.gymcrm.context.UserContextHolder;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.facade.GymFacade;
import org.gymcrm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade gymCrmFacade = context.getBean(GymFacade.class);

            logger.info("Registering new Trainee");
            User userTrainee = new User();
            userTrainee.setFirstName("John");
            userTrainee.setLastName("Smith");

            Trainee newTrainee = new Trainee();
            newTrainee.setUser(userTrainee);
            newTrainee.setDateOfBirth(LocalDate.of(2001, 3, 10));
            newTrainee.setAddress("Ternopil");

            Trainee createdTrainee = gymCrmFacade.createTrainee(newTrainee);

            String traineeUsername = createdTrainee.getUser().getUsername();
            String traineePassword = createdTrainee.getUser().getPassword();
            logger.info("Successfully registered! Username: {}, Password: {}", traineeUsername, traineePassword);

            UserContextHolder.setCredentials(traineeUsername, traineePassword);

            try {
                logger.info("Accessing Protected Data (getAllTrainees)");
                gymCrmFacade.getAllTrainees().forEach(t ->
                        logger.info("Trainee found - Username: {}, Name: {} {}",
                                t.getUser().getUsername(), t.getUser().getFirstName(), t.getUser().getLastName())
                );

                logger.info("Registering new Trainer");
                User userTrainer = new User();
                userTrainer.setFirstName("Alex");
                userTrainer.setLastName("Brown");

                Trainer trainerModel = new Trainer();
                trainerModel.setUser(userTrainer);

                TrainingType spec = new TrainingType();
                spec.setId(1L);
                trainerModel.setSpecialization(spec);

                Trainer createdTrainer = gymCrmFacade.createTrainer(trainerModel);
                logger.info("Trainer registered with username: {}", createdTrainer.getUser().getUsername());

                logger.info("Creating Training Session (Protected)");
                Training newTraining = new Training();
                newTraining.setTrainee(createdTrainee);
                newTraining.setTrainer(createdTrainer);
                newTraining.setTrainingType(spec);
                newTraining.setTrainingName("Personal Strength Training");
                newTraining.setTrainingDate(LocalDate.of(2026, 6, 30));
                newTraining.setTrainingDuration(90);

                gymCrmFacade.createTraining(newTraining);
                logger.info("Training session 'Personal Strength Training' created successfully!");

            } finally {
                UserContextHolder.clear();
                logger.debug("Security context cleared from ThreadLocal.");
            }

            logger.info("Gym CRM Application finished successfully.");
        } catch (AuthenticationException e) {
            logger.warn("Security Violation: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Application encountered an unexpected error during execution", e);
        }
    }
}