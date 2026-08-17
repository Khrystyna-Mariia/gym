package org.gymcrm.workload.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.validation.Valid;
import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class WorkloadEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadEventListener.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final TrainerWorkloadService workloadService;

    public WorkloadEventListener(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @JmsListener(destination = "${jms.workload-queue:workload.events}", containerFactory = "jmsListenerContainerFactory")
    public void onWorkloadEvent(@Valid WorkloadRequest event, Message rawMessage) {
        String transactionId = extractTransactionId(rawMessage);
        try {
            MDC.put(TRANSACTION_ID_KEY, transactionId);
            logger.info("Received workload event: trainer={}, action={}, duration={}",
                    event.trainerUsername(), event.actionType(), event.trainingDuration());

            workloadService.processWorkload(event);
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private String extractTransactionId(Message rawMessage) {
        try {
            String txId = rawMessage.getStringProperty(TRANSACTION_ID_HEADER);
            return txId != null ? txId : "N/A";
        } catch (JMSException e) {
            return "N/A";
        }
    }
}