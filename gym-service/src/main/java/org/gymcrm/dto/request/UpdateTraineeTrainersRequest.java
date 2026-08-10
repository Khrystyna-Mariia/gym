package org.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotEmpty(message = "Trainers list must not be empty") List<@NotBlank String> trainerUsernames
) {}