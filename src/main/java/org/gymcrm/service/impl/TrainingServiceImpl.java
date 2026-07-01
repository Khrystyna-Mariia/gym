package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
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
            throw new ValidationException("Training must not be null");
        }

        if (training.getTraineeId() == null || traineeService.selectById(training.getTraineeId()).isEmpty()) {
            logger.warn("Failed to create training: trainee with id {} does not exist", training.getTraineeId());
            throw new EntityNotFoundException("Trainee with id " + training.getTraineeId() + " does not exist");
        }

        if (training.getTrainerId() == null || trainerService.selectById(training.getTrainerId()).isEmpty()) {
            logger.warn("Failed to create training: trainer with id {} does not exist", training.getTrainerId());
            throw new EntityNotFoundException("Trainer with id " + training.getTrainerId() + " does not exist");
        }

        return trainingDao.save(training);
    }

    @Override
    public Optional<Training> selectById(Long id) {
        return trainingDao.findById(id);
    }

    @Override
    public List<Training> selectAll() {
        return trainingDao.findAll();
    }
}