package org.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StorageConfigTest {

    @Test
    void shouldCreateStorageBeans() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(StorageConfig.class)) {

            Map<?, ?> traineeStorage = context.getBean("traineeStorage", Map.class);
            Map<?, ?> trainerStorage = context.getBean("trainerStorage", Map.class);
            Map<?, ?> trainingStorage = context.getBean("trainingStorage", Map.class);
            Map<?, ?> trainingTypeStorage = context.getBean("trainingTypeStorage", Map.class);

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
}