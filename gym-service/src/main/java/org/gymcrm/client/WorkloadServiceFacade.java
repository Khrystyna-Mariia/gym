package org.gymcrm.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WorkloadServiceFacade {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadServiceFacade.class);
    private static final String CIRCUIT_BREAKER_NAME = "workloadService";

    private final WorkloadServiceClient workloadServiceClient;

    public WorkloadServiceFacade(WorkloadServiceClient workloadServiceClient) {
        this.workloadServiceClient = workloadServiceClient;
    }

    @Async("workloadReporterExecutor")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void sendWorkload(WorkloadRequest request) {
        workloadServiceClient.sendWorkload(request);
        logger.info("Workload event sent: trainer={}, action={}, duration={}",
                request.trainerUsername(), request.actionType(), request.trainingDuration());
    }

    private void fallback(WorkloadRequest request, Throwable ex) {
        logger.error("Workload service unavailable, event NOT delivered for trainer={} action={}: {}",
                request.trainerUsername(), request.actionType(), ex.toString());
    }
}