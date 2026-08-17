package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.event.WorkloadReportEvent;
import org.gymcrm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadReporterImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TrainingWorkloadReporterImpl reporter;

    @BeforeEach
    void setUp() {
        reporter = new TrainingWorkloadReporterImpl(trainingDao, eventPublisher);
    }

    @Test
    void report_shouldConstructCorrectEventAndPublish() {
        Training training = createTraining(1L, "john.coach", "John", "Coach", true);

        reporter.report(training, ActionType.ADD);

        ArgumentCaptor<WorkloadReportEvent> captor = ArgumentCaptor.forClass(WorkloadReportEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        WorkloadRequest request = captor.getValue().payload();
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

        ArgumentCaptor<WorkloadReportEvent> captor = ArgumentCaptor.forClass(WorkloadReportEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<WorkloadReportEvent> events = captor.getAllValues();
        assertEquals(2, events.size());

        assertEquals("trainer.one", events.get(0).payload().trainerUsername());
        assertEquals(ActionType.DELETE, events.get(0).payload().actionType());

        assertEquals("trainer.two", events.get(1).payload().trainerUsername());
        assertEquals(ActionType.DELETE, events.get(1).payload().actionType());
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