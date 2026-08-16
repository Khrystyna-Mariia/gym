package org.gymcrm.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.filter.TransactionLogFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadEventPublisherTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message message;

    private WorkloadEventPublisher publisher;
    private static final String QUEUE_NAME = "workload.events";

    @BeforeEach
    void setUp() {
        publisher = new WorkloadEventPublisher(jmsTemplate, QUEUE_NAME);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void publish_WhenTransactionIdPresentInMdc_SetsTransactionHeaderAndSends() throws JMSException {
        String txId = "tx-12345";
        MDC.put(TransactionLogFilter.TRANSACTION_ID_KEY, txId);

        WorkloadRequest request = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 9, 15), 60, ActionType.ADD
        );

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        publisher.publish(request);

        verify(jmsTemplate).convertAndSend(eq(QUEUE_NAME), eq(request), postProcessorCaptor.capture());

        MessagePostProcessor postProcessor = postProcessorCaptor.getValue();
        postProcessor.postProcessMessage(message);

        verify(message).setStringProperty(TransactionLogFilter.TRANSACTION_ID_HEADER, txId);
    }

    @Test
    void publish_WhenTransactionIdMissingInMdc_DoesNotSetHeaderAndSends() throws JMSException {
        WorkloadRequest request = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 9, 15), 60, ActionType.ADD
        );

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        publisher.publish(request);

        verify(jmsTemplate).convertAndSend(eq(QUEUE_NAME), eq(request), postProcessorCaptor.capture());

        MessagePostProcessor postProcessor = postProcessorCaptor.getValue();
        postProcessor.postProcessMessage(message);

        verify(message, never()).setStringProperty(eq(TransactionLogFilter.TRANSACTION_ID_HEADER), anyString());
    }
}