package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainer;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final UserProfileInitializer userProfileInitializer;

    public TrainerServiceImpl(TrainerDao trainerDao, UserProfileInitializer userProfileInitializer) {
        this.trainerDao = trainerDao;
        this.userProfileInitializer = userProfileInitializer;
    }

    @Override
    public Trainer create(Trainer trainer) {
        if (trainer == null || trainer.getUser() == null) {
            logger.warn("Failed to create trainer profile: trainer or user data is null");
            throw new ValidationException("Trainer and associated User must not be null");
        }

        userProfileInitializer.initialize(trainer.getUser());
        return trainerDao.save(trainer);
    }

    @Override
    public Trainer update(Trainer trainer) {
        return trainerDao.update(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> selectById(Long id) {
        return trainerDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> selectAll() {
        return trainerDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> selectByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerDao.findTrainersNotAssignedToTrainee(traineeUsername);
    }
}