package org.gymcrm.dao.impl;

import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.storage.InMemoryIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoImplTest {
    private TrainerDaoImpl trainerDao;
    private Map<Long, Trainer> trainerStorage;

    @BeforeEach
    void setUp() {
        trainerStorage = new HashMap<>();

        trainerDao = new TrainerDaoImpl();
        trainerDao.setTrainerStorage(trainerStorage);
        trainerDao.setIdGenerator(new InMemoryIdGenerator());
    }

    @Test
    void shouldSaveTrainerWithExistingId() {
        Trainer trainer = createTrainer(1L, "Michael", "Green");

        Trainer savedTrainer = trainerDao.save(trainer);

        assertEquals(trainer, savedTrainer);
        assertEquals(1, trainerStorage.size());
        assertTrue(trainerStorage.containsKey(1L));
    }

    @Test
    void shouldGenerateIdWhenSavingTrainerWithoutId() {
        Trainer existingTrainer = createTrainer(5L, "Michael", "Green");
        trainerStorage.put(5L, existingTrainer);

        Trainer newTrainer = createTrainer(null, "Olivia", "White");

        Trainer savedTrainer = trainerDao.save(newTrainer);

        assertEquals(6L, savedTrainer.getUserId());
        assertEquals(2, trainerStorage.size());
        assertTrue(trainerStorage.containsKey(6L));
        assertEquals(newTrainer, trainerStorage.get(6L));
    }

    @Test
    void shouldGenerateIdOneWhenStorageIsEmpty() {
        Trainer trainer = createTrainer(null, "Olivia", "White");

        Trainer savedTrainer = trainerDao.save(trainer);

        assertEquals(1L, savedTrainer.getUserId());
        assertEquals(1, trainerStorage.size());
        assertTrue(trainerStorage.containsKey(1L));
    }

    @Test
    void shouldNotRegenerateIdWhenSavingTrainerWithExistingId() {
        Trainer trainer = createTrainer(10L, "Olivia", "White");

        Trainer savedTrainer = trainerDao.save(trainer);

        assertEquals(10L, savedTrainer.getUserId());
        assertTrue(trainerStorage.containsKey(10L));
    }

    @Test
    void shouldFindTrainerById() {
        Trainer trainer = createTrainer(1L, "Michael", "Green");
        trainerStorage.put(1L, trainer);

        Optional<Trainer> result = trainerDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainerNotFound() {
        Optional<Trainer> result = trainerDao.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllTrainers() {
        Trainer firstTrainer = createTrainer(1L, "Michael", "Green");
        Trainer secondTrainer = createTrainer(2L, "Olivia", "White");

        trainerStorage.put(1L, firstTrainer);
        trainerStorage.put(2L, secondTrainer);

        List<Trainer> result = trainerDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainer));
        assertTrue(result.contains(secondTrainer));
    }

    @Test
    void shouldUpdateExistingTrainer() {
        Trainer oldTrainer = createTrainer(1L, "Michael", "Green");
        trainerStorage.put(1L, oldTrainer);

        Trainer updatedTrainer = new Trainer(
                1L,
                "Michael",
                "Green",
                "Michael.Green",
                "newPassword",
                true,
                new TrainingType(2L, "Yoga")
        );

        Trainer result = trainerDao.update(updatedTrainer);

        assertEquals(updatedTrainer, result);
        assertEquals(1L, result.getUserId());
        assertEquals("Michael", result.getFirstName());
        assertEquals("Green", result.getLastName());
        assertEquals("Michael.Green", result.getUsername());
        assertTrue(result.isActive());
        assertEquals("Yoga", result.getSpecialization().getTrainingTypeName());
        assertEquals(2L, result.getSpecialization().getId());

        assertEquals("Yoga", trainerStorage.get(1L).getSpecialization().getTrainingTypeName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTrainer() {
        Trainer trainer = createTrainer(99L, "Michael", "Green");

        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(trainer));
    }

    private Trainer createTrainer(Long userId, String firstName, String lastName) {
        return new Trainer(
                userId,
                firstName,
                lastName,
                firstName + "." + lastName,
                "password123",
                true,
                new TrainingType(1L, "Fitness")
        );
    }
}