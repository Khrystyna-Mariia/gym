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
        logger.info("Facade: creating trainee {} {}", trainee.getFirstName(), trainee.getLastName());
        return traineeService.create(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: updating trainee with id {}", trainee.getUserId());
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long userId) {
        logger.info("Facade: deleting trainee with id {}", userId);
        traineeService.delete(userId);
    }

    public Optional<Trainee> getTrainee(Long userId) {
        logger.info("Facade: retrieving trainee with id {}", userId);
        return traineeService.selectById(userId);
    }

    public List<Trainee> getAllTrainees() {
        logger.info("Facade: retrieving all trainees");
        return traineeService.selectAll();
    }

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Facade: creating trainer {} {}", trainer.getFirstName(), trainer.getLastName());
        return trainerService.create(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: updating trainer with id {}", trainer.getUserId());
        return trainerService.update(trainer);
    }

    public Optional<Trainer> getTrainer(Long userId) {
        logger.info("Facade: retrieving trainer with id {}", userId);
        return trainerService.selectById(userId);
    }
    public List<Trainer> getAllTrainers() {
        logger.info("Facade: retrieving all trainers");
        return trainerService.selectAll();
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
}
