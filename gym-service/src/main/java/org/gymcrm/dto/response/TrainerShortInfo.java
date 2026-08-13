package org.gymcrm.dto.response;

public record TrainerShortInfo(
        String username,
        String firstName,
        String lastName,
        String specialization
) {}