package org.gymcrm.dao.impl;

import org.gymcrm.model.Training;
import org.gymcrm.storage.InMemoryIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoImplTest {
    private TrainingDaoImpl trainingDao;
    private Map<Long, Training> trainingStorage;
    private InMemoryIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        trainingStorage = new HashMap<>();
        idGenerator = new InMemoryIdGenerator();

        trainingDao = new TrainingDaoImpl();
        trainingDao.setTrainingStorage(trainingStorage);
        trainingDao.setIdGenerator(idGenerator);
    }

    @Test
    void shouldSaveTrainingWithExistingId() {
        Training training = createTraining(1L, "Morning Fitness");

        Training savedTraining = trainingDao.save(training);

        assertEquals(training, savedTraining);
        assertEquals(1, trainingStorage.size());
        assertTrue(trainingStorage.containsKey(1L));
    }

    @Test
    void shouldGenerateIdWhenSavingTrainingWithoutId() {
        Training existingTraining = createTraining(5L, "Morning Fitness");
        trainingStorage.put(5L, existingTraining);
        idGenerator.initializeMaxTrainingId(existingTraining.getId());

        Training newTraining = createTraining(null, "Evening Yoga");

        Training savedTraining = trainingDao.save(newTraining);

        assertEquals(6L, savedTraining.getId());
        assertEquals(2, trainingStorage.size());
        assertTrue(trainingStorage.containsKey(6L));
        assertEquals(newTraining, trainingStorage.get(6L));
    }

    @Test
    void shouldGenerateIdOneWhenStorageIsEmpty() {
        Training training = createTraining(null, "Evening Yoga");

        Training savedTraining = trainingDao.save(training);

        assertEquals(1L, savedTraining.getId());
        assertEquals(1, trainingStorage.size());
        assertTrue(trainingStorage.containsKey(1L));
    }

    @Test
    void shouldNotRegenerateIdWhenSavingTrainingWithExistingId() {
        Training training = createTraining(10L, "Evening Yoga");

        Training savedTraining = trainingDao.save(training);

        assertEquals(10L, savedTraining.getId());
        assertTrue(trainingStorage.containsKey(10L));
    }

    @Test
    void shouldFindTrainingById() {
        Training training = createTraining(1L, "Morning Fitness");
        trainingStorage.put(1L, training);

        Optional<Training> result = trainingDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainingNotFound() {
        Optional<Training> result = trainingDao.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllTrainings() {
        Training firstTraining = createTraining(1L, "Morning Fitness");
        Training secondTraining = createTraining(2L, "Evening Yoga");

        trainingStorage.put(1L, firstTraining);
        trainingStorage.put(2L, secondTraining);

        List<Training> result = trainingDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTraining));
        assertTrue(result.contains(secondTraining));
    }

    private Training createTraining(Long id, String trainingName) {
        return new Training(
                id,
                1L,
                1L,
                trainingName,
                1L,
                LocalDate.of(2026, 6, 24),
                60
        );
    }
}