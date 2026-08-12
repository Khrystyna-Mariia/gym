package org.gymcrm.client;

import org.gymcrm.dto.workload.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service")
public interface WorkloadServiceClient {

    @PostMapping("/api/v1/workload")
    void sendWorkload(@RequestBody WorkloadRequest request);
}