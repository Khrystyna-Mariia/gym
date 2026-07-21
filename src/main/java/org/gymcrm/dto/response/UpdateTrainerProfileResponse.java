package org.gymcrm.dto.response;

import java.util.List;

public record UpdateTrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        boolean isActive,
        List<TraineeShortInfo> trainees
) {}