package org.gymcrm.workload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.gymcrm.workload.model.ActionType;

import java.time.LocalDate;

public record WorkloadRequest(

        @NotBlank(message = "Trainer username is required")
        String trainerUsername,

        @NotBlank(message = "Trainer first name is required")
        String trainerFirstName,

        @NotBlank(message = "Trainer last name is required")
        String trainerLastName,

        @NotNull(message = "isActive flag is required")
        Boolean isActive,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @Positive(message = "Training duration must be greater than 0")
        int trainingDuration,

        @NotNull(message = "Action type is required")
        ActionType actionType
) {}