package org.gymcrm.service.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final UserProfileInitializer userProfileInitializer;

    public TraineeServiceImpl(TraineeDao traineeDao, UserProfileInitializer userProfileInitializer) {
        this.traineeDao = traineeDao;
        this.userProfileInitializer = userProfileInitializer;
    }

    @Override
    public Trainee create(Trainee trainee) {
        if (trainee == null) {
            logger.warn("Failed to create trainee profile: trainee is null");
            throw new IllegalArgumentException("Trainee must not be null");
        }

        logger.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());
        userProfileInitializer.initialize(trainee);

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee profile created successfully with id {}", savedTrainee.getUserId());
        return savedTrainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        logger.info("Updating trainee profile with id {}", trainee.getUserId());
        return traineeDao.update(trainee);
    }

    @Override
    public void delete(Long userId) {
        logger.info("Deleting trainee profile with id {}", userId);

        boolean deleted = traineeDao.deleteById(userId);

        if (deleted) {
            logger.info("Trainee profile with id {} deleted successfully", userId);
        } else {
            logger.warn("Trainee profile with id {} was not found and was not deleted", userId);
        }
    }

    @Override
    public Optional<Trainee> selectById(Long userId) {
        logger.info("Selecting trainee profile with id {}", userId);

        Optional<Trainee> trainee = traineeDao.findById(userId);

        if (trainee.isEmpty()) {
            logger.warn("Trainee profile with id {} was not found", userId);
        }

        return trainee;
    }

    @Override
    public List<Trainee> selectAll() {
        logger.info("Selecting all trainee profiles");
        return traineeDao.findAll();
    }
}