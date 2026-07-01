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

        userProfileInitializer.initialize(trainer);
        return trainerDao.save(trainer);
    }

    @Override
    public Trainer update(Trainer trainer) {
        return trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> selectById(Long userId) {
        return trainerDao.findById(userId);
    }

    @Override
    public List<Trainer> selectAll() {
        return trainerDao.findAll();
    }
}