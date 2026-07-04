package org.gymcrm.facade;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(Trainee trainee) {
        logger.info("Facade: creating trainee {} {}", trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        return traineeService.create(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: updating trainee with id {}", trainee.getId());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        logger.info("Facade: deleting trainee with id {}", id);
        traineeService.delete(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        logger.info("Facade: retrieving trainee with id {}", id);
        return traineeService.selectById(id);
    }

    public Optional<Trainee> getTraineeByUsername(String username) {
        logger.info("Facade: retrieving trainee with username {}", username);
        return traineeService.selectByUsername(username);
    }

    public List<Trainee> getAllTrainees() {
        logger.info("Facade: retrieving all trainees");
        return traineeService.selectAll();
    }

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Facade: creating trainer {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        return trainerService.create(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: updating trainer with id {}", trainer.getId());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> getTrainer(Long id) {
        logger.info("Facade: retrieving trainer with id {}", id);
        return trainerService.selectById(id);
    }

    public Optional<Trainer> getTrainerByUsername(String username) {
        logger.info("Facade: retrieving trainer with username {}", username);
        return trainerService.selectByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        logger.info("Facade: retrieving all trainers");
        return trainerService.selectAll();
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        logger.info("Facade: retrieving unassigned trainers for trainee {}", traineeUsername);
        return trainerService.getUnassignedTrainers(traineeUsername);
    }

    public Training createTraining(Training training) {
        logger.info("Facade: creating training {}", training.getTrainingName());
        return trainingService.create(training);
    }

    public Optional<Training> getTraining(Long id) {
        logger.info("Facade: selecting training with id {}", id);
        return trainingService.selectById(id);
    }

    public List<Training> getAllTrainings() {
        logger.info("Facade: selecting all trainings");
        return trainingService.selectAll();
    }

    public List<Training> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainer, String type) {
        logger.info("Facade: retrieving trainee trainings for user {}", username);
        return trainingService.getTraineeTrainings(username, from, to, trainer, type);
    }

    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String trainee) {
        logger.info("Facade: retrieving trainer trainings for user {}", username);
        return trainingService.getTrainerTrainings(username, from, to, trainee);
    }
}
