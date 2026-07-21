package org.gymcrm.dto.response;

import java.time.LocalDate;
import java.util.List;

public record UpdateTraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerShortInfo> trainers
) {}