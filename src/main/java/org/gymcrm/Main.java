package org.gymcrm;

import org.gymcrm.config.AppConfig;
import org.gymcrm.facade.GymFacade;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            // Application logic can be added here
            GymFacade gymCrmFacade = context.getBean(GymFacade.class);

            System.out.println("Initial trainees:");
            gymCrmFacade.getAllTrainees().forEach(System.out::println);

            Trainee newTrainee = new Trainee(
                    null,
                    "John",
                    "Smith",
                    null,
                    null,
                    true,
                    LocalDate.of(2001, 3, 10),
                    "Ternopil"
            );

            gymCrmFacade.createTrainee(newTrainee);

            System.out.println("\nTrainees after creating new trainee:");
            gymCrmFacade.getAllTrainees().forEach(System.out::println);

            Trainer newTrainer = new Trainer(
                    null,
                    "John",
                    "Smith",
                    null,
                    null,
                    true,
                    new TrainingType(3L, "Strength")
            );

            gymCrmFacade.createTrainer(newTrainer);

            System.out.println("\nTrainers after creating new trainer:");
            gymCrmFacade.getAllTrainers().forEach(System.out::println);

            Training newTraining = new Training(
                    null,
                    3L,
                    3L,
                    "Personal Strength Training",
                    3L,
                    LocalDate.of(2026, 6, 30),
                    90
            );

            gymCrmFacade.createTraining(newTraining);

            System.out.println("\nTrainings after creating new training:");
            gymCrmFacade.getAllTrainings().forEach(System.out::println);

            System.out.println("\nSelect trainee by id 3:");
            gymCrmFacade.getTrainee(3L).ifPresent(System.out::println);

            System.out.println("\nGym CRM Application finished successfully.");
        }
    }
}