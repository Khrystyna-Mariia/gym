package org.gymcrm.service.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.exception.ValidationException;
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
            throw new ValidationException("Trainee must not be null");
        }

        userProfileInitializer.initialize(trainee);
        return traineeDao.save(trainee);
    }

    @Override
    public Trainee update(Trainee trainee) {
        return traineeDao.update(trainee);
    }

    @Override
    public void delete(Long userId) {
        boolean deleted = traineeDao.deleteById(userId);

        if (!deleted) {
            logger.warn("Trainee profile with id {} was not found and was not deleted", userId);
        }
    }

    @Override
    public Optional<Trainee> selectById(Long userId) {
        return traineeDao.findById(userId);
    }

    @Override
    public List<Trainee> selectAll() {
        return traineeDao.findAll();
    }
}