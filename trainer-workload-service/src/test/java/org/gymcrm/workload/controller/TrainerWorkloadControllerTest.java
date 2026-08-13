package org.gymcrm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.model.ActionType;
import org.gymcrm.workload.security.ServiceTokenValidator;
import org.gymcrm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerWorkloadService workloadService;

    @MockitoBean
    private ServiceTokenValidator tokenValidator;

    @Test
    void acceptWorkload_ValidRequest_Returns200OK() throws Exception {
        WorkloadRequest request = new WorkloadRequest(
                "david.miller", "David", "Miller", true,
                LocalDate.of(2026, 9, 12), 60, ActionType.ADD
        );

        doNothing().when(workloadService).processWorkload(any(WorkloadRequest.class));

        mockMvc.perform(post("/api/v1/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void acceptWorkload_InvalidRequest_Returns400BadRequest() throws Exception {
        WorkloadRequest invalidRequest = new WorkloadRequest(
                "", "", "Miller", true, null, -10, null
        );

        mockMvc.perform(post("/api/v1/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWorkload_TrainerExists_Returns200AndSummary() throws Exception {
        WorkloadSummaryResponse summary = new WorkloadSummaryResponse(
                "david.miller", "David", "Miller", true, List.of()
        );

        when(workloadService.getSummary("david.miller")).thenReturn(Optional.of(summary));

        mockMvc.perform(get("/api/v1/workload/david.miller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("david.miller"))
                .andExpect(jsonPath("$.firstName").value("David"));
    }

    @Test
    void getWorkload_TrainerNotFound_Returns404() throws Exception {
        when(workloadService.getSummary("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/workload/unknown"))
                .andExpect(status().isNotFound());
    }
}