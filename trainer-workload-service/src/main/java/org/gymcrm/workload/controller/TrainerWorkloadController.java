package org.gymcrm.workload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.service.TrainerWorkloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workload")
@Tag(name = "Trainer Workload", description = "Accepts training workload events and returns monthly summaries")
public class TrainerWorkloadController {

    private final TrainerWorkloadService workloadService;

    public TrainerWorkloadController(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Accept a training workload event (ADD or DELETE)")
    public void acceptWorkload(@Valid @RequestBody WorkloadRequest request) {
        workloadService.processWorkload(request);
    }

    @GetMapping("/{trainerUsername}")
    @Operation(summary = "Get monthly workload summary for a trainer")
    public ResponseEntity<WorkloadSummaryResponse> getWorkload(@PathVariable String trainerUsername) {
        return workloadService.getSummary(trainerUsername)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search trainer workload summaries by first and last name")
    public ResponseEntity<List<WorkloadSummaryResponse>> search(@RequestParam String firstName,
                                                                @RequestParam String lastName) {
        return ResponseEntity.ok(workloadService.search(firstName, lastName));
    }
}