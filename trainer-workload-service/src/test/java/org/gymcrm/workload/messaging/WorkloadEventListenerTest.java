package org.gymcrm.workload.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.model.ActionType;
import org.gymcrm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadEventListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @Mock
    private Message rawMessage;

    private final WorkloadEventListener listener = new WorkloadEventListener(mock(TrainerWorkloadService.class));

    @Test
    void onWorkloadEvent_delegatesToServiceAndClearsMdcAfterward() throws JMSException {
        WorkloadEventListener listenerWithMock = new WorkloadEventListener(workloadService);
        WorkloadRequest event = new WorkloadRequest(
                "john.doe", "John", "Doe", true, LocalDate.of(2026, 9, 15), 60, ActionType.ADD);
        when(rawMessage.getStringProperty("X-Transaction-Id")).thenReturn("tx-123");

        listenerWithMock.onWorkloadEvent(event, rawMessage);

        verify(workloadService).processWorkload(event);
        assertNull(MDC.get("transactionId"), "MDC must be cleared after processing");
    }

    @Test
    void onWorkloadEvent_usesFallbackTransactionIdWhenHeaderMissing() throws JMSException {
        WorkloadEventListener listenerWithMock = new WorkloadEventListener(workloadService);
        WorkloadRequest event = new WorkloadRequest(
                "john.doe", "John", "Doe", true, LocalDate.of(2026, 9, 15), 60, ActionType.ADD);
        when(rawMessage.getStringProperty("X-Transaction-Id")).thenReturn(null);

        listenerWithMock.onWorkloadEvent(event, rawMessage);

        verify(workloadService).processWorkload(event);
    }

    @Test
    void onWorkloadEvent_mdcClearedEvenWhenServiceThrows() throws JMSException {
        WorkloadEventListener listenerWithMock = new WorkloadEventListener(workloadService);
        WorkloadRequest event = new WorkloadRequest(
                "john.doe", "John", "Doe", true, LocalDate.of(2026, 9, 15), 60, ActionType.ADD);
        when(rawMessage.getStringProperty("X-Transaction-Id")).thenReturn("tx-456");
        doThrow(new RuntimeException("DB unavailable")).when(workloadService).processWorkload(event);

        try {
            listenerWithMock.onWorkloadEvent(event, rawMessage);
        } catch (RuntimeException ignored) {
        }

        assertNull(MDC.get("transactionId"), "MDC must be cleared even when downstream call fails");
    }
}