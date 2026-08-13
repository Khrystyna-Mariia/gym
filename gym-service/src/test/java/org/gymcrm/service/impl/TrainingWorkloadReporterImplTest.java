package org.gymcrm.service.impl;

import org.gymcrm.client.WorkloadServiceFacade;
import org.gymcrm.dao.TrainingDao;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadReporterImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private WorkloadServiceFacade workloadServiceFacade;

    private TrainingWorkloadReporterImpl reporter;

    @BeforeEach
    void setUp() {
        reporter = new TrainingWorkloadReporterImpl(trainingDao, workloadServiceFacade);
    }

    @Test
    void report_shouldConstructCorrectRequestAndSendToFacade() {
        Training training = createTraining(1L, "john.coach", "John", "Coach", true);

        reporter.report(training, ActionType.ADD);

        ArgumentCaptor<WorkloadRequest> captor = ArgumentCaptor.forClass(WorkloadRequest.class);
        verify(workloadServiceFacade).sendWorkload(captor.capture());

        WorkloadRequest request = captor.getValue();
        assertEquals("john.coach", request.trainerUsername());
        assertEquals("John", request.trainerFirstName());
        assertEquals("Coach", request.trainerLastName());
        assertTrue(request.isActive());
        assertEquals(LocalDate.of(2026, 8, 12), request.trainingDate());
        assertEquals(60, request.trainingDuration());
        assertEquals(ActionType.ADD, request.actionType());
    }

    @Test
    void reportCascadedDeletionsForTrainee_shouldFindTrainingsAndReportDeleteActionForEach() {
        Long traineeId = 100L;
        Training t1 = createTraining(1L, "trainer.one", "Trainer", "One", true);
        Training t2 = createTraining(2L, "trainer.two", "Trainer", "Two", false);

        when(trainingDao.findByTraineeId(traineeId)).thenReturn(List.of(t1, t2));

        reporter.reportCascadedDeletionsForTrainee(traineeId);

        verify(trainingDao).findByTraineeId(traineeId);

        ArgumentCaptor<WorkloadRequest> captor = ArgumentCaptor.forClass(WorkloadRequest.class);
        verify(workloadServiceFacade, times(2)).sendWorkload(captor.capture());

        List<WorkloadRequest> requests = captor.getAllValues();
        assertEquals(2, requests.size());

        assertEquals("trainer.one", requests.get(0).trainerUsername());
        assertEquals(ActionType.DELETE, requests.get(0).actionType());

        assertEquals("trainer.two", requests.get(1).trainerUsername());
        assertEquals(ActionType.DELETE, requests.get(1).actionType());
    }

    private Training createTraining(Long id, String trainerUsername, String firstName, String lastName, boolean active) {
        User user = new User(10L, firstName, lastName, trainerUsername, "pass", active, Role.TRAINER);
        Trainer trainer = new Trainer();
        trainer.setId(10L);
        trainer.setUser(user);

        Trainee trainee = new Trainee();
        trainee.setId(100L);

        TrainingType type = new TrainingType(1L, TrainingTypeEnum.FITNESS);

        return new Training(
                id,
                trainee,
                trainer,
                "Fitness Class",
                type,
                LocalDate.of(2026, 8, 12),
                60
        );
    }
}