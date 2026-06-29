package org.gymcrm.config;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(StorageConfig.class)
class StorageConfigTest {

    @Autowired
    @Qualifier("traineeStorage")
    private Map<Long, Trainee> traineeStorage;

    @Autowired
    @Qualifier("trainerStorage")
    private Map<Long, Trainer> trainerStorage;

    @Autowired
    @Qualifier("trainingStorage")
    private Map<Long, Training> trainingStorage;

    @Autowired
    @Qualifier("trainingTypeStorage")
    private Map<Long, TrainingType> trainingTypeStorage;

    @Test
    void shouldCreateStorageBeans() {
        assertNotNull(traineeStorage);
        assertNotNull(trainerStorage);
        assertNotNull(trainingStorage);
        assertNotNull(trainingTypeStorage);

        assertTrue(traineeStorage.isEmpty());
        assertTrue(trainerStorage.isEmpty());
        assertTrue(trainingStorage.isEmpty());
        assertTrue(trainingTypeStorage.isEmpty());
    }
}