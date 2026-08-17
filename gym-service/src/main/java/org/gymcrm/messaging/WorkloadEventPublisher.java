package org.gymcrm.messaging;

import org.gymcrm.dto.workload.WorkloadRequest;
import org.gymcrm.filter.TransactionLogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkloadEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadEventPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final String queueName;

    public WorkloadEventPublisher(JmsTemplate jmsTemplate,
                                  @Value("${jms.workload-queue:workload.events}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    public void publish(WorkloadRequest event) {
        jmsTemplate.convertAndSend(queueName, event, message -> {
            String transactionId = MDC.get(TransactionLogFilter.TRANSACTION_ID_KEY);
            if (transactionId != null) {
                message.setStringProperty(TransactionLogFilter.TRANSACTION_ID_HEADER, transactionId);
            }
            return message;
        });
        logger.info("Published workload event to queue '{}': trainer={}, action={}, duration={}",
                queueName, event.trainerUsername(), event.actionType(), event.trainingDuration());
    }
}