package org.gymcrm.storage;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class InitialDataParser {
    private static final Logger logger = LoggerFactory.getLogger(InitialDataParser.class);

    public TrainingType parseTrainingType(String[] parts) {
        return new TrainingType(Long.parseLong(parts[1]), parts[2]);
    }

    public Trainee parseTrainee(String[] parts) {
        BaseUserData user = parseUserFields(parts);
        return new Trainee(
                user.userId(), user.firstName(), user.lastName(),
                user.username(), user.password(), user.isActive(),
                LocalDate.parse(parts[7]), parts[8]
        );
    }

    public Trainer parseTrainer(String[] parts, Map<Long, TrainingType> trainingTypeStorage) {
        BaseUserData user = parseUserFields(parts);
        Long specializationId = Long.parseLong(parts[7]);
        TrainingType specialization = trainingTypeStorage.get(specializationId);

        if (specialization == null) {
            logger.error("Training type with id {} was not found for trainer {} {}",
                    specializationId, user.firstName(), user.lastName());
            throw new IllegalStateException("Training type with id " + specializationId + " was not found");
        }

        return new Trainer(
                user.userId(), user.firstName(), user.lastName(),
                user.username(), user.password(), user.isActive(),
                specialization
        );
    }

    public Training parseTraining(String[] parts) {
        return new Training(
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                Long.parseLong(parts[3]),
                parts[4],
                Long.parseLong(parts[5]),
                LocalDate.parse(parts[6]),
                Integer.parseInt(parts[7])
        );
    }

    private BaseUserData parseUserFields(String[] parts) {
        return new BaseUserData(
                Long.parseLong(parts[1]),
                parts[2], parts[3], parts[4], parts[5],
                Boolean.parseBoolean(parts[6])
        );
    }

    private record BaseUserData(Long userId, String firstName, String lastName,
                                String username, String password, boolean isActive) {
    }
}