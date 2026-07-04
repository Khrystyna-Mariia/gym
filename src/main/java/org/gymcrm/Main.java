package org.gymcrm;

import org.gymcrm.config.AppConfig;
import org.gymcrm.facade.GymFacade;
import org.gymcrm.model.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            // Application logic can be added here
            GymFacade gymCrmFacade = context.getBean(GymFacade.class);

            System.out.println("Initial trainees:");
            gymCrmFacade.getAllTrainees().forEach(t -> System.out.println(t.getUser().getFirstName() + " " + t.getUser().getLastName()));

            User userTrainee = new User();
            userTrainee.setFirstName("John");
            userTrainee.setLastName("Smith");

            Trainee newTrainee = new Trainee();
            newTrainee.setUser(userTrainee);
            newTrainee.setDateOfBirth(LocalDate.of(2001, 3, 10));
            newTrainee.setAddress("Ternopil");

            gymCrmFacade.createTrainee(newTrainee);

            System.out.println("\nTrainees after creating new trainee:");
            gymCrmFacade.getAllTrainees().forEach(t -> System.out.println(t.getUser().getFirstName() + " " + t.getUser().getLastName()));

            User userTrainer = new User();
            userTrainer.setFirstName("Alex");
            userTrainer.setLastName("Brown");

            Trainer newTrainer = new Trainer();
            newTrainer.setUser(userTrainer);

            TrainingType spec = new TrainingType();
            spec.setId(1L);
            newTrainer.setSpecialization(spec);

            gymCrmFacade.createTrainer(newTrainer);

            Training newTraining = new Training();
            newTraining.setTrainee(newTrainee);
            newTraining.setTrainer(newTrainer);
            newTraining.setTrainingType(spec);

            newTraining.setTrainingName("Personal Strength Training");
            newTraining.setTrainingDate(LocalDate.of(2026, 6, 30));
            newTraining.setTrainingDuration(90);

            gymCrmFacade.createTraining(newTraining);

            System.out.println("\nGym CRM Application finished successfully.");
        }
    }
}