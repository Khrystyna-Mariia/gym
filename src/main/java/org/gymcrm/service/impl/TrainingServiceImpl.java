package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.model.Training;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingDao trainingDao;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TrainingServiceImpl(TrainingDao trainingDao, TraineeService traineeService, TrainerService trainerService) {
        this.trainingDao = trainingDao;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Override
    public Training create(Training training) {
        if (training == null) {
            logger.warn("Failed to create training: training is null");
            throw new IllegalArgumentException("Training must not be null");
        }

        if (training.getTraineeId() == null || traineeService.selectById(training.getTraineeId()).isEmpty()) {
            logger.warn("Failed to create training: trainee with id {} does not exist", training.getTraineeId());
            throw new IllegalArgumentException("Trainee with id " + training.getTraineeId() + " does not exist");
        }

        if (training.getTrainerId() == null || trainerService.selectById(training.getTrainerId()).isEmpty()) {
            logger.warn("Failed to create training: trainer with id {} does not exist", training.getTrainerId());
            throw new IllegalArgumentException("Trainer with id " + training.getTrainerId() + " does not exist");
        }

        logger.info("Creating training profile with name {}", training.getTrainingName());
        Training savedTraining = trainingDao.save(training);
        logger.info("Training profile created successfully with id {}", savedTraining.getId());

        return savedTraining;
    }

    @Override
    public Optional<Training> selectById(Long id) {
        logger.info("Selecting training profile with id {}", id);

        Optional<Training> training = trainingDao.findById(id);

        if (training.isEmpty()) {
            logger.warn("Training profile with id {} was not found", id);
        }

        return training;
    }

    @Override
    public List<Training> selectAll() {
        logger.info("Selecting all training profiles");
        return trainingDao.findAll();
    }
}
