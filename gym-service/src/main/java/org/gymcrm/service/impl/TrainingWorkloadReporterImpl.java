package org.gymcrm.service.impl;

import org.gymcrm.client.WorkloadServiceFacade;
import org.gymcrm.dao.TrainingDao;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
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
    private final WorkloadServiceFacade workloadServiceFacade;

    public TrainingWorkloadReporterImpl(TrainingDao trainingDao, WorkloadServiceFacade workloadServiceFacade) {
        this.trainingDao = trainingDao;
        this.workloadServiceFacade = workloadServiceFacade;
    }

    @Override
    public void report(Training training, ActionType actionType) {
        var trainer = training.getTrainer();
        var request = new WorkloadRequest(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
        workloadServiceFacade.sendWorkload(request);
    }

    @Override
    public void reportCascadedDeletionsForTrainee(Long traineeId) {
        List<Training> trainings = trainingDao.findByTraineeId(traineeId);
        trainings.forEach(t -> report(t, ActionType.DELETE));
        logger.info("Reported {} cascaded training deletion(s) to workload service for trainee id={}",
                trainings.size(), traineeId);
    }
}