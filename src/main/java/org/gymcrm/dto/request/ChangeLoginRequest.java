package org.gymcrm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeLoginRequest(
        @NotBlank(message = "Old password is required") String oldPassword,
        @NotBlank(message = "New password is required") String newPassword
) {}