package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.messaging.WorkloadEventPublisher;
import org.gymcrm.model.Training;
import org.gymcrm.service.TrainingWorkloadReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingWorkloadReporterImpl implements TrainingWorkloadReporter {

    private static final Logger logger = LoggerFactory.getLogger(TrainingWorkloadReporterImpl.class);

    private final TrainingDao trainingDao;
    private final WorkloadEventPublisher workloadEventPublisher;

    public TrainingWorkloadReporterImpl(TrainingDao trainingDao, WorkloadEventPublisher workloadEventPublisher) {
        this.trainingDao = trainingDao;
        this.workloadEventPublisher = workloadEventPublisher;
    }

    @Override
    public void report(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        var event = new WorkloadRequest(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
        workloadEventPublisher.publish(event);
    }

    @Override
    public void reportCascadedDeletionsForTrainee(Long traineeId) {
        List<Training> trainings = trainingDao.findByTraineeId(traineeId);
        trainings.forEach(t -> report(t, ActionType.DELETE));
        logger.info("Published {} cascaded training deletion event(s) to workload queue for trainee id={}",
                trainings.size(), traineeId);
    }
}