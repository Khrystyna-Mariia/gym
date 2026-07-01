package org.gymcrm.storage;

import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class InitialDataParser {
    private static final int TRAINING_TYPE_FIELD_COUNT = 3;
    private static final int TRAINEE_FIELD_COUNT = 9;
    private static final int TRAINER_FIELD_COUNT = 8;
    private static final int TRAINING_FIELD_COUNT = 8;

    public TrainingType parseTrainingType(String[] parts) {
        validateFieldCount(parts, TRAINING_TYPE_FIELD_COUNT, "TRAINING_TYPE");

        return new TrainingType(
                parseLong(parts[1], "training type id"),
                required(parts[2], "training type name")
        );
    }

    public Trainee parseTrainee(String[] parts) {
        validateFieldCount(parts, TRAINEE_FIELD_COUNT, "TRAINEE");

        BaseUserData user = parseUserFields(parts);

        return new Trainee(
                user.userId(),
                user.firstName(),
                user.lastName(),
                user.username(),
                user.password(),
                user.isActive(),
                parseDate(parts[7], "date of birth"),
                required(parts[8], "address")
        );
    }

    public Trainer parseTrainer(String[] parts, Map<Long, TrainingType> trainingTypeStorage) {
        validateFieldCount(parts, TRAINER_FIELD_COUNT, "TRAINER");

        BaseUserData user = parseUserFields(parts);
        Long specializationId = parseLong(parts[7], "specialization id");

        TrainingType specialization = trainingTypeStorage.get(specializationId);

        if (specialization == null) {
            throw new ValidationException("Training type with id " + specializationId
                    + " was not found for trainer " + user.firstName() + " " + user.lastName());
        }

        return new Trainer(
                user.userId(),
                user.firstName(),
                user.lastName(),
                user.username(),
                user.password(),
                user.isActive(),
                specialization
        );
    }

    public Training parseTraining(String[] parts) {
        validateFieldCount(parts, TRAINING_FIELD_COUNT, "TRAINING");

        return new Training(
                parseLong(parts[1], "training id"),
                parseLong(parts[2], "trainee id"),
                parseLong(parts[3], "trainer id"),
                required(parts[4], "training name"),
                parseLong(parts[5], "training type id"),
                parseDate(parts[6], "training date"),
                parseInt(parts[7], "training duration")
        );
    }

    private BaseUserData parseUserFields(String[] parts) {
        return new BaseUserData(
                parseLong(parts[1], "user id"),
                required(parts[2], "first name"),
                required(parts[3], "last name"),
                required(parts[4], "username"),
                required(parts[5], "password"),
                parseBoolean(parts[6], "active status")
        );
    }

    private void validateFieldCount(String[] parts, int expectedFieldCount, String recordType) {
        if (parts == null || parts.length != expectedFieldCount) {
            throw new ValidationException(String.format(
                    "Malformed data for %s. Expected %d fields, but got %d",
                    recordType,
                    expectedFieldCount,
                    parts == null ? 0 : parts.length
            ));
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Required field is empty: " + fieldName);
        }

        return value.trim();
    }

    private Long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(required(value, fieldName));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid long value for field " + fieldName + ": " + value, e);
        }
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(required(value, fieldName));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid integer value for field " + fieldName + ": " + value, e);
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(required(value, fieldName));
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date value for field " + fieldName + ": " + value, e);
        }
    }

    private boolean parseBoolean(String value, String fieldName) {
        String normalizedValue = required(value, fieldName).toLowerCase();

        if (!normalizedValue.equals("true") && !normalizedValue.equals("false")) {
            throw new ValidationException("Invalid boolean value for field " + fieldName + ": " + value);
        }

        return Boolean.parseBoolean(normalizedValue);
    }

    private record BaseUserData(
            Long userId,
            String firstName,
            String lastName,
            String username,
            String password,
            boolean isActive
    ) {
    }
}