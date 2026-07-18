package org.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerRegistrationRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotNull(message = "Specialization is required") Long specializationId
) {}