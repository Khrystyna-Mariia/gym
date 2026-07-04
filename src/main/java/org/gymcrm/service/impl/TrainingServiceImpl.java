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
    public Training create(Training training) {
        if (training == null) {
            logger.warn("Failed to create training: training is null");
            throw new ValidationException("Training must not be null");
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

        return trainingDao.save(training);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> selectById(Long id) {
        return trainingDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> selectAll() {
        return trainingDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String typeName) {
        return trainingDao.findTraineeTrainings(username, from, to, trainerName, typeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        return trainingDao.findTrainerTrainings(username, from, to, traineeName);
    }
}