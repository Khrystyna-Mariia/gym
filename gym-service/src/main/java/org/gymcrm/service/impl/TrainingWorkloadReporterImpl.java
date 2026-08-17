package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.event.WorkloadReportEvent;
import org.gymcrm.model.Training;
import org.gymcrm.service.TrainingWorkloadReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingWorkloadReporterImpl implements TrainingWorkloadReporter {

    private static final Logger logger = LoggerFactory.getLogger(TrainingWorkloadReporterImpl.class);

    private final TrainingDao trainingDao;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingWorkloadReporterImpl(TrainingDao trainingDao, ApplicationEventPublisher eventPublisher) {
        this.trainingDao = trainingDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void report(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        WorkloadReportEvent event = WorkloadReportEvent.of(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
        eventPublisher.publishEvent(event);
    }

    @Override
    public void reportCascadedDeletionsForTrainee(Long traineeId) {
        List<Training> trainings = trainingDao.findByTraineeId(traineeId);
        trainings.forEach(t -> report(t, ActionType.DELETE));
        logger.info("Registered {} cascaded training deletion event(s) for trainee id={}, to be published after transaction commit",
                trainings.size(), traineeId);
    }
}