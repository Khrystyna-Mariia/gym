package org.gymcrm.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotBlank(message = "Training name is required") String trainingName,
        @NotNull(message = "Training date is required") LocalDate trainingDate,
        @Positive(message = "Training duration must be greater than 0") int trainingDuration
) {}