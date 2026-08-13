package org.gymcrm.service;

import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.model.Training;

public interface TrainingWorkloadReporter {
    void report(Training training, ActionType actionType);
    void reportCascadedDeletionsForTrainee(Long traineeId);
}