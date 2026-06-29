package org.gymcrm.dao.impl;

import org.gymcrm.model.Trainee;
import org.gymcrm.storage.InMemoryIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoImplTest {
    private TraineeDaoImpl traineeDao;
    private Map<Long, Trainee> traineeStorage;
    private InMemoryIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        traineeStorage = new HashMap<>();
        idGenerator = new InMemoryIdGenerator();

        traineeDao = new TraineeDaoImpl();
        traineeDao.setTraineeStorage(traineeStorage);
        traineeDao.setIdGenerator(idGenerator);
    }

    @Test
    void shouldSaveTraineeWithExistingId() {
        Trainee trainee = createTrainee(1L, "John", "Smith");

        Trainee savedTrainee = traineeDao.save(trainee);

        assertEquals(trainee, savedTrainee);
        assertEquals(1, traineeStorage.size());
        assertTrue(traineeStorage.containsKey(1L));
        assertEquals("John.Smith", traineeStorage.get(1L).getUsername());
    }

    @Test
    void shouldGenerateIdWhenSavingTraineeWithoutId() {
        Trainee existingTrainee = createTrainee(5L, "John", "Smith");
        traineeStorage.put(5L, existingTrainee);
        idGenerator.initializeMaxTraineeId(existingTrainee.getUserId());

        Trainee newTrainee = createTrainee(null, "Anna", "Brown");

        Trainee savedTrainee = traineeDao.save(newTrainee);

        assertEquals(6L, savedTrainee.getUserId());
        assertEquals(2, traineeStorage.size());
        assertTrue(traineeStorage.containsKey(6L));
        assertEquals(newTrainee, traineeStorage.get(6L));
    }

    @Test
    void shouldGenerateIdOneWhenStorageIsEmpty() {
        Trainee trainee = createTrainee(null, "Anna", "Brown");

        Trainee savedTrainee = traineeDao.save(trainee);

        assertEquals(1L, savedTrainee.getUserId());
        assertEquals(1, traineeStorage.size());
        assertTrue(traineeStorage.containsKey(1L));
    }

    @Test
    void shouldNotRegenerateIdWhenSavingTraineeWithExistingId() {
        Trainee trainee = createTrainee(10L, "Anna", "Brown");

        Trainee savedTrainee = traineeDao.save(trainee);

        assertEquals(10L, savedTrainee.getUserId());
        assertTrue(traineeStorage.containsKey(10L));
    }

    @Test
    void shouldFindTraineeById() {
        Trainee trainee = createTrainee(1L, "John", "Smith");
        traineeStorage.put(1L, trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenTraineeNotFound() {
        Optional<Trainee> result = traineeDao.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllTrainees() {
        Trainee firstTrainee = createTrainee(1L, "John", "Smith");
        Trainee secondTrainee = createTrainee(2L, "Anna", "Brown");

        traineeStorage.put(1L, firstTrainee);
        traineeStorage.put(2L, secondTrainee);

        List<Trainee> result = traineeDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainee));
        assertTrue(result.contains(secondTrainee));
    }

    @Test
    void shouldUpdateExistingTrainee() {
        Trainee oldTrainee = createTrainee(1L, "John", "Smith");
        traineeStorage.put(1L, oldTrainee);

        Trainee updatedTrainee = new Trainee(
                1L,
                "John",
                "Smith",
                "John.Smith",
                "newPassword",
                true,
                LocalDate.of(2001, 5, 10),
                "Lviv"
        );

        Trainee result = traineeDao.update(updatedTrainee);

        assertEquals(updatedTrainee, result);
        assertEquals(1L, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("John.Smith", result.getUsername());
        assertTrue(result.isActive());
        assertEquals("Lviv", result.getAddress());
        assertEquals(LocalDate.of(2001, 5, 10), result.getDateOfBirth());

        assertEquals("Lviv", traineeStorage.get(1L).getAddress());
        assertEquals(LocalDate.of(2001, 5, 10), traineeStorage.get(1L).getDateOfBirth());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTrainee() {
        Trainee trainee = createTrainee(99L, "John", "Smith");

        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(trainee));
    }

    @Test
    void shouldDeleteTraineeById() {
        Trainee trainee = createTrainee(1L, "John", "Smith");
        traineeStorage.put(1L, trainee);

        traineeDao.deleteById(1L);

        assertFalse(traineeStorage.containsKey(1L));
        assertTrue(traineeStorage.isEmpty());
    }

    private Trainee createTrainee(Long userId, String firstName, String lastName) {
        return new Trainee(
                userId,
                firstName,
                lastName,
                firstName + "." + lastName,
                "password123",
                true,
                LocalDate.of(2000, 1, 1),
                "Kyiv"
        );
    }
}