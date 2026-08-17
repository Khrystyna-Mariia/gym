package org.gymcrm.event;

import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.messaging.WorkloadEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadEventTransactionListenerTest {

    @Mock
    private WorkloadEventPublisher workloadEventPublisher;

    @InjectMocks
    private WorkloadEventTransactionListener listener;

    @Test
    void onWorkloadReportEvent_shouldPublishPayloadToActiveMQ() {
        WorkloadReportEvent event = WorkloadReportEvent.of(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 8, 17), 60, ActionType.ADD
        );

        listener.onWorkloadReportEvent(event);

        verify(workloadEventPublisher).publish(event.payload());
    }

    @Test
    void onWorkloadReportEvent_shouldCatchAndLogExceptionWhenPublisherFails() {
        WorkloadReportEvent event = WorkloadReportEvent.of(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 8, 17), 60, ActionType.ADD
        );

        doThrow(new RuntimeException("JMS connection failed"))
                .when(workloadEventPublisher).publish(any(WorkloadRequest.class));

        assertDoesNotThrow(() -> listener.onWorkloadReportEvent(event));

        verify(workloadEventPublisher).publish(event.payload());
    }
}