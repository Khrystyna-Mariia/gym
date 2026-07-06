package org.gymcrm.init;

import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InitialDataParserTest {
    private InitialDataParser parser;

    @BeforeEach
    void setUp() {
        parser = new InitialDataParser();
    }

    @Test
    void shouldParseTrainingType() {
        String[] parts = {"TRAINING_TYPE", "1", "Fitness"};

        TrainingType result = parser.parseTrainingType(parts);

        assertEquals(1L, result.getId());
        assertEquals(TrainingTypeEnum.FITNESS, result.getTrainingTypeName());
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

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("Alex", result.getUser().getFirstName());
        assertEquals("Gomez", result.getUser().getLastName());
        assertEquals("Alex.Gomez", result.getUser().getUsername());
        assertEquals("password123", result.getUser().getPassword());
        assertTrue(result.getUser().isActive());
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

        Trainer result = parser.parseTrainer(parts);

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("David", result.getUser().getFirstName());
        assertEquals("Miller", result.getUser().getLastName());
        assertEquals("David.Miller", result.getUser().getUsername());
        assertEquals("password789", result.getUser().getPassword());
        assertTrue(result.getUser().isActive());
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

        assertNotNull(result);
        assertEquals("Morning Fitness", result.getTrainingName());
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

}