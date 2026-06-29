package org.gymcrm.dao.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.model.Training;
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
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private Map<Long, Training> trainingStorage;
    private InMemoryIdGenerator idGenerator;

    @Autowired
    public void setIdGenerator(InMemoryIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Autowired
    public void setTrainingStorage(@Qualifier("trainingStorage") Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @Override
    public Training save(Training training) {
        if (training.getId() == null) {
            Long generatedId = idGenerator.generateNextTrainingId();
            training.setId(generatedId);
            logger.debug("Generated training id {}", generatedId);
        }

        logger.debug("Saving training with id {}", training.getId());
        trainingStorage.put(training.getId(), training);

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        logger.debug("Finding training with id {}", id);
        return Optional.ofNullable(trainingStorage.get(id));
    }

    @Override
    public List<Training> findAll() {
        logger.debug("Finding all trainings");
        return new ArrayList<>(trainingStorage.values());
    }
}