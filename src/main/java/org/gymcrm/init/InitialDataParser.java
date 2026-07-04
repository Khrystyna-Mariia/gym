package org.gymcrm.init;

import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class InitialDataParser {
    private static final int TRAINING_TYPE_FIELD_COUNT = 3;
    private static final int TRAINEE_FIELD_COUNT = 9;
    private static final int TRAINER_FIELD_COUNT = 8;
    private static final int TRAINING_FIELD_COUNT = 8;

    public TrainingType parseTrainingType(String[] parts) {
        validateFieldCount(parts, TRAINING_TYPE_FIELD_COUNT, "TRAINING_TYPE");

        TrainingType type = new TrainingType();
        type.setTrainingTypeName(required(parts[2], "training type name"));
        return type;
    }

    public Trainee parseTrainee(String[] parts) {
        validateFieldCount(parts, TRAINEE_FIELD_COUNT, "TRAINEE");

        User user = parseUserFields(parts);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(parseDate(parts[7], "date of birth"));
        trainee.setAddress(required(parts[8], "address"));
        return trainee;
    }

    public Trainer parseTrainer(String[] parts) {
        validateFieldCount(parts, TRAINER_FIELD_COUNT, "TRAINER");

        User user = parseUserFields(parts);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        return trainer;
    }

    public Training parseTraining(String[] parts) {
        validateFieldCount(parts, TRAINING_FIELD_COUNT, "TRAINING");

        Training training = new Training();
        training.setTrainingName(required(parts[4], "training name"));
        training.setTrainingDate(parseDate(parts[6], "training date"));
        training.setTrainingDuration(parseInt(parts[7], "training duration"));

        return training;
    }

    private User parseUserFields(String[] parts) {
        User user = new User();
        user.setFirstName(required(parts[2], "first name"));
        user.setLastName(required(parts[3], "last name"));
        user.setUsername(required(parts[4], "username"));
        user.setPassword(required(parts[5], "password"));
        user.setActive(parseBoolean(parts[6], "active status"));
        return user;
    }

    private void validateFieldCount(String[] parts, int expectedFieldCount, String recordType) {
        if (parts == null || parts.length != expectedFieldCount) {
            throw new ValidationException(String.format(
                    "Malformed data for %s. Expected %d fields, but got %d",
                    recordType, expectedFieldCount, parts == null ? 0 : parts.length
            ));
        }
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Required field is empty: " + fieldName);
        }
        return value.trim();
    }

    public Long parseLong(String value, String fieldName) {
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
}