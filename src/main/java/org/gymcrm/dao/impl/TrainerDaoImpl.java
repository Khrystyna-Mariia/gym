package org.gymcrm.dao.impl;

import org.gymcrm.dao.TrainerDao;
import org.gymcrm.model.Trainer;
import org.gymcrm.storage.InMemoryIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private Map<Long, Trainer> trainerStorage;
    private InMemoryIdGenerator idGenerator;

    @Autowired
    public void setIdGenerator(InMemoryIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Autowired
    public void setTrainerStorage(@Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer.getUserId() == null) {
            Long generatedId = idGenerator.generateNextId(trainerStorage.values(), Trainer::getUserId);
            trainer.setUserId(generatedId);
            logger.debug("Generated trainer id {}", generatedId);
        }

        logger.debug("Saving trainer with id {}", trainer.getUserId());
        trainerStorage.put(trainer.getUserId(), trainer);

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Long userId = trainer.getUserId();

        if (!trainerStorage.containsKey(userId)) {
            logger.warn("Cannot update trainer. Trainer with id {} was not found", userId);
            throw new IllegalArgumentException("Trainer with id " + userId + " was not found");
        }

        logger.debug("Updating trainer with id {}", userId);
        trainerStorage.put(userId, trainer);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long userId) {
        logger.debug("Finding trainer with id {}", userId);
        return Optional.ofNullable(trainerStorage.get(userId));
    }

    @Override
    public List<Trainer> findAll() {
        logger.debug("Finding all trainers");
        return new ArrayList<>(trainerStorage.values());
    }
}