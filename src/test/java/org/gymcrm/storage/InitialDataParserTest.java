package org.gymcrm.storage;

import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InitialDataParserTest {
    private InitialDataParser parser;
    private Map<Long, TrainingType> trainingTypeStorage;

    @BeforeEach
    void setUp() {
        parser = new InitialDataParser();

        trainingTypeStorage = new HashMap<>();
        trainingTypeStorage.put(1L, new TrainingType(1L, "Fitness"));
    }

    @Test
    void shouldParseTrainingType() {
        String[] parts = {"TRAINING_TYPE", "1", "Fitness"};

        TrainingType result = parser.parseTrainingType(parts);

        assertEquals(1L, result.getId());
        assertEquals("Fitness", result.getTrainingTypeName());
    }

    @Test
    void shouldParseTrainee() {
        String[] parts = {
                "TRAINEE",
                "1",
                "Alex",
                "Gomez",
                "Alex.Gomez",
                "password123",
                "true",
                "2000-05-15",
                "Kyiv"
        };

        Trainee result = parser.parseTrainee(parts);

        assertEquals(1L, result.getUserId());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Gomez", result.getLastName());
        assertEquals("Alex.Gomez", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertTrue(result.isActive());
        assertEquals(LocalDate.of(2000, 5, 15), result.getDateOfBirth());
        assertEquals("Kyiv", result.getAddress());
    }

    @Test
    void shouldParseTrainer() {
        String[] parts = {
                "TRAINER",
                "1",
                "David",
                "Miller",
                "David.Miller",
                "password789",
                "true",
                "1"
        };

        Trainer result = parser.parseTrainer(parts, trainingTypeStorage);

        assertEquals(1L, result.getUserId());
        assertEquals("David", result.getFirstName());
        assertEquals("Miller", result.getLastName());
        assertEquals("David.Miller", result.getUsername());
        assertEquals("password789", result.getPassword());
        assertTrue(result.isActive());
        assertEquals(1L, result.getSpecialization().getId());
        assertEquals("Fitness", result.getSpecialization().getTrainingTypeName());
    }

    @Test
    void shouldParseTraining() {
        String[] parts = {
                "TRAINING",
                "1",
                "1",
                "1",
                "Morning Fitness",
                "1",
                "2026-06-23",
                "60"
        };

        Training result = parser.parseTraining(parts);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getTraineeId());
        assertEquals(1L, result.getTrainerId());
        assertEquals("Morning Fitness", result.getTrainingName());
        assertEquals(1L, result.getTrainingTypeId());
        assertEquals(LocalDate.of(2026, 6, 23), result.getTrainingDate());
        assertEquals(60, result.getTrainingDuration());
    }

    @Test
    void shouldThrowExceptionWhenTrainingTypeHasInvalidFieldCount() {
        String[] parts = {"TRAINING_TYPE", "1"};

        assertThrows(ValidationException.class, () -> parser.parseTrainingType(parts));
    }

    @Test
    void shouldThrowExceptionWhenTraineeHasEmptyRequiredField() {
        String[] parts = {
                "TRAINEE",
                "1",
                "",
                "Gomez",
                "Alex.Gomez",
                "password123",
                "true",
                "2000-05-15",
                "Kyiv"
        };

        assertThrows(ValidationException.class, () -> parser.parseTrainee(parts));
    }

    @Test
    void shouldThrowExceptionWhenTraineeDateIsInvalid() {
        String[] parts = {
                "TRAINEE",
                "1",
                "Alex",
                "Gomez",
                "Alex.Gomez",
                "password123",
                "true",
                "invalid-date",
                "Kyiv"
        };

        assertThrows(ValidationException.class, () -> parser.parseTrainee(parts));
    }

    @Test
    void shouldThrowExceptionWhenTrainingIdIsInvalid() {
        String[] parts = {
                "TRAINING",
                "abc",
                "1",
                "1",
                "Morning Fitness",
                "1",
                "2026-06-23",
                "60"
        };

        assertThrows(ValidationException.class, () -> parser.parseTraining(parts));
    }

    @Test
    void shouldThrowExceptionWhenTrainingDurationIsInvalid() {
        String[] parts = {
                "TRAINING",
                "1",
                "1",
                "1",
                "Morning Fitness",
                "1",
                "2026-06-23",
                "sixty"
        };

        assertThrows(ValidationException.class, () -> parser.parseTraining(parts));
    }

    @Test
    void shouldThrowExceptionWhenBooleanValueIsInvalid() {
        String[] parts = {
                "TRAINEE",
                "1",
                "Alex",
                "Gomez",
                "Alex.Gomez",
                "password123",
                "yes",
                "2000-05-15",
                "Kyiv"
        };

        assertThrows(ValidationException.class, () -> parser.parseTrainee(parts));
    }

    @Test
    void shouldThrowExceptionWhenTrainerSpecializationDoesNotExist() {
        String[] parts = {
                "TRAINER",
                "1",
                "David",
                "Miller",
                "David.Miller",
                "password789",
                "true",
                "99"
        };

        assertThrows(ValidationException.class, () -> parser.parseTrainer(parts, trainingTypeStorage));
    }
}