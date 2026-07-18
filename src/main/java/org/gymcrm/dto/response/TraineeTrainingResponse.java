package org.gymcrm.dto.response;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String trainerName
) {}