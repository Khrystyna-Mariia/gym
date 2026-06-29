package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainer;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        if (trainer == null) {
            logger.warn("Failed to create trainer profile: trainer is null");
            throw new ValidationException("Trainer must not be null");
        }

        logger.info("Creating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());

        userProfileInitializer.initialize(trainer);

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer profile created successfully with id {}", savedTrainer.getUserId());

        return savedTrainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        logger.info("Updating trainer profile with id {}", trainer.getUserId());
        return trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> selectById(Long userId) {
        logger.info("Selecting trainer profile with id {}", userId);
        return trainerDao.findById(userId);
    }

    @Override
    public List<Trainer> selectAll() {
        logger.info("Selecting all trainer profiles");
        return trainerDao.findAll();
    }
}