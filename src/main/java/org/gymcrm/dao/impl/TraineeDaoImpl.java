package org.gymcrm.dao.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.storage.InMemoryIdGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TraineeDaoImpl.class);

    private Map<Long, Trainee> traineeStorage;
    private InMemoryIdGenerator idGenerator;

    @Autowired
    public void setTraineeStorage(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    public void setIdGenerator(InMemoryIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee.getUserId() == null) {
            Long generatedId = idGenerator.generateNextTraineeId();
            trainee.setUserId(generatedId);
            logger.debug("Generated trainee id {}", generatedId);
        }

        logger.debug("Saving trainee with id {}", trainee.getUserId());
        traineeStorage.put(trainee.getUserId(), trainee);

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Long userId = trainee.getUserId();

        if (!traineeStorage.containsKey(userId)) {
            logger.warn("Cannot update trainee. Trainee with id {} was not found", userId);
            throw new IllegalArgumentException("Trainee with id " + userId + " was not found");
        }

        logger.debug("Updating trainee with id {}", userId);
        traineeStorage.put(userId, trainee);

        return trainee;
    }

    @Override
    public boolean deleteById(Long userId) {
        if (userId == null) {
            logger.warn("Cannot delete trainee: id is null");
            throw new IllegalArgumentException("Trainee id must not be null");
        }

        Trainee removedTrainee = traineeStorage.remove(userId);

        if (removedTrainee == null) {
            logger.warn("Cannot delete trainee. Trainee with id {} was not found", userId);
            return false;
        }

        logger.debug("Deleting trainee with id {}", userId);
        return true;
    }

    @Override
    public Optional<Trainee> findById(Long userId) {
        logger.debug("Finding trainee with id {}", userId);
        Trainee trainee = traineeStorage.get(userId);
        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Finding all trainees");
        return new ArrayList<>(traineeStorage.values());
    }

}
