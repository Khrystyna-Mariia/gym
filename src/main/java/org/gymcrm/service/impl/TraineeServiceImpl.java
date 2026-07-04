package org.gymcrm.service.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainee;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
        if (trainee == null || trainee.getUser() == null) {
            logger.warn("Failed to create trainee profile: trainee or user data is null");
            throw new ValidationException("Trainee and associated User must not be null");
        }

        userProfileInitializer.initialize(trainee.getUser());
        return traineeDao.save(trainee);
    }

    @Override
    public Trainee update(Trainee trainee) {
        return traineeDao.update(trainee);
    }

    @Override
    public void delete(Long id) {
        boolean deleted = traineeDao.deleteById(id);

        if (!deleted) {
            logger.warn("Trainee profile with id {} was not found and was not deleted", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> selectById(Long id) {
        return traineeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> selectAll() {
        return traineeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> selectByUsername(String username) {
        return traineeDao.findByUsername(username);
    }
}