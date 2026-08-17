package org.gymcrm.event;

import org.gymcrm.messaging.WorkloadEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkloadEventTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadEventTransactionListener.class);

    private final WorkloadEventPublisher workloadEventPublisher;

    public WorkloadEventTransactionListener(WorkloadEventPublisher workloadEventPublisher) {
        this.workloadEventPublisher = workloadEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkloadReportEvent(WorkloadReportEvent event) {
        try {
            workloadEventPublisher.publish(event.payload());
        } catch (Exception e) {
            logger.error("Failed to publish workload event AFTER successful commit for trainer={}: {}",
                    event.payload().trainerUsername(), e.getMessage(), e);
        }
    }
}