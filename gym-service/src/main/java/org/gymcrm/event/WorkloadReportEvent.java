package org.gymcrm.event;

import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;

public record WorkloadReportEvent(WorkloadRequest payload) {

    public static WorkloadReportEvent of(String trainerUsername, String trainerFirstName, String trainerLastName,
                                         boolean isActive, java.time.LocalDate trainingDate,
                                         int trainingDuration, ActionType actionType) {
        return new WorkloadReportEvent(new WorkloadRequest(
                trainerUsername, trainerFirstName, trainerLastName, isActive,
                trainingDate, trainingDuration, actionType));
    }
}