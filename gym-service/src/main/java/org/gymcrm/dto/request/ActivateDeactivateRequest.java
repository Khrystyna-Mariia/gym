package org.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivateDeactivateRequest(
        @NotBlank(message = "Username is required") String username,
        @NotNull(message = "isActive flag is required") Boolean isActive
) {}