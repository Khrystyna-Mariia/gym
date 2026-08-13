package org.gymcrm.client;

import org.gymcrm.dto.workload.ActionType;
import org.gymcrm.dto.workload.WorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class WorkloadServiceFacadeTest {

    @Autowired
    private WorkloadServiceFacade workloadServiceFacade;

    @MockitoBean
    private WorkloadServiceClient workloadServiceClient;

    @Test
    void sendWorkload_shouldCallServiceClientSuccessfully() {
        WorkloadRequest request = createWorkloadRequest();

        doNothing().when(workloadServiceClient).sendWorkload(request);

        assertDoesNotThrow(() -> workloadServiceFacade.sendWorkload(request));

        verify(workloadServiceClient, timeout(2000).times(1)).sendWorkload(request);
    }

    @Test
    void sendWorkload_shouldTriggerFallbackAndNotThrowException_whenServiceClientFails() {
        WorkloadRequest request = createWorkloadRequest();

        doThrow(new RuntimeException("Remote service unavailable"))
                .when(workloadServiceClient).sendWorkload(any(WorkloadRequest.class));

        assertDoesNotThrow(() -> workloadServiceFacade.sendWorkload(request));

        verify(workloadServiceClient, timeout(3000).atLeastOnce()).sendWorkload(request);
    }

    private WorkloadRequest createWorkloadRequest() {
        return new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 8, 12), 60, ActionType.ADD
        );
    }
}