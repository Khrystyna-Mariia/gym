package org.gymcrm.service.impl;

import org.gymcrm.annotation.RequireAuth;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
    @RequireAuth
    public Training create(Training training) {
        validateTraining(training);
        return trainingDao.save(training);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public Optional<Training> selectById(Long id) {
        return trainingDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Training> selectAll() {
        return trainingDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Training> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String typeName) {
        return trainingDao.findTraineeTrainings(username, from, to, trainerName, typeName);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        return trainingDao.findTrainerTrainings(username, from, to, traineeName);
    }

    private void validateTraining(Training training) {
        if (training == null) {
            throw new ValidationException("Training data must not be null");
        }

        if (training.getTrainingName() == null || training.getTrainingName().isBlank()) {
            throw new ValidationException("Training name is required and cannot be empty");
        }
        if (training.getTrainingDate() == null) {
            throw new ValidationException("Training date is required");
        }
        if (training.getTrainingDuration() <= 0) {
            throw new ValidationException("Training duration must be a positive number greater than zero");
        }
        if (training.getTrainingType() == null || training.getTrainingType().getId() == null) {
            throw new ValidationException("Training type with a valid ID is required");
        }

        if (training.getTrainee() == null || training.getTrainee().getId() == null ||
                traineeService.selectById(training.getTrainee().getId()).isEmpty()) {
            logger.warn("Failed to create training: trainee does not exist");
            throw new EntityNotFoundException("Trainee does not exist");
        }

        if (training.getTrainer() == null || training.getTrainer().getId() == null ||
                trainerService.selectById(training.getTrainer().getId()).isEmpty()) {
            logger.warn("Failed to create training: trainer does not exist");
            throw new EntityNotFoundException("Trainer does not exist");
        }
    }
}