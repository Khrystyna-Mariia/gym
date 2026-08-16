package org.gymcrm.workload.service;

import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.mapper.WorkloadMapper;
import org.gymcrm.workload.model.ActionType;
import org.gymcrm.workload.model.MonthlyWorkload;
import org.gymcrm.workload.model.TrainerWorkload;
import org.gymcrm.workload.model.YearlyWorkload;
import org.gymcrm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @Mock
    private WorkloadMapper mapper;

    @InjectMocks
    private TrainerWorkloadService workloadService;

    private WorkloadRequest addEvent;

    @BeforeEach
    void setUp() {
        addEvent = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 9, 15), 60, ActionType.ADD
        );
    }

    @Test
    void processWorkload_NewTrainer_CreatesAndSavesWorkload() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());

        workloadService.processWorkload(addEvent);

        verify(repository).save(argThat(trainer -> {
            assertEquals("john.doe", trainer.getTrainerUsername());
            assertEquals("John", trainer.getFirstName());
            assertEquals("Doe", trainer.getLastName());
            assertTrue(trainer.isActive());
            assertEquals(1, trainer.getYears().size());
            assertEquals(2026, trainer.getYears().get(0).getYear());
            assertEquals(1, trainer.getYears().get(0).getMonths().size());
            assertEquals(9, trainer.getYears().get(0).getMonths().get(0).getMonth());
            assertEquals(60, trainer.getYears().get(0).getMonths().get(0).getSummaryDuration());
            return true;
        }));
    }

    @Test
    void processWorkload_ExistingYearAndMonth_AddsToExistingDuration() {
        TrainerWorkload existing = new TrainerWorkload();
        existing.setTrainerUsername("john.doe");
        YearlyWorkload year = new YearlyWorkload();
        year.setYear(2026);
        MonthlyWorkload month = new MonthlyWorkload();
        month.setMonth(9);
        month.setSummaryDuration(30);
        year.getMonths().add(month);
        existing.getYears().add(year);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));

        workloadService.processWorkload(addEvent);

        assertEquals(90, month.getSummaryDuration());
    }

    @Test
    void processWorkload_DeleteAction_ClampsToZeroWhenNegative() {
        TrainerWorkload existingWorkload = new TrainerWorkload();
        existingWorkload.setTrainerUsername("john.doe");

        YearlyWorkload year = new YearlyWorkload();
        year.setYear(2026);
        year.setTrainerWorkload(existingWorkload);

        MonthlyWorkload month = new MonthlyWorkload();
        month.setMonth(9);
        month.setSummaryDuration(30);
        month.setYearlyWorkload(year);

        year.getMonths().add(month);
        existingWorkload.getYears().add(year);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existingWorkload));

        WorkloadRequest deleteEvent = new WorkloadRequest(
                "john.doe", "John", "Doe", true, LocalDate.of(2026, 9, 15), 60, ActionType.DELETE
        );

        workloadService.processWorkload(deleteEvent);

        assertEquals(0, month.getSummaryDuration());
        verify(repository).save(existingWorkload);
    }

    @Test
    void getSummary_TrainerExists_ReturnsSummary() {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setTrainerUsername("john.doe");

        WorkloadSummaryResponse expectedResponse = new WorkloadSummaryResponse(
                "john.doe", "John", "Doe", true, List.of()
        );

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(mapper.toResponse(workload)).thenReturn(expectedResponse);

        Optional<WorkloadSummaryResponse> result = workloadService.getSummary("john.doe");

        assertTrue(result.isPresent());
        assertEquals(expectedResponse, result.get());
    }

    @Test
    void getSummary_TrainerNotFound_ReturnsEmptyOptional() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        Optional<WorkloadSummaryResponse> result = workloadService.getSummary("unknown");

        assertTrue(result.isEmpty());
    }
}