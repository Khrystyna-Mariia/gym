package org.gymcrm.workload.mapper;

import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.model.MonthlyWorkload;
import org.gymcrm.workload.model.TrainerWorkload;
import org.gymcrm.workload.model.YearlyWorkload;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadMapperTest {

    private final WorkloadMapper mapper = new WorkloadMapper();

    @Test
    void toResponse_SortsYearsAndMonthsCorrectly() {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setTrainerUsername("test.trainer");
        workload.setFirstName("Test");
        workload.setLastName("Trainer");
        workload.setActive(true);

        YearlyWorkload y2026 = new YearlyWorkload();
        y2026.setYear(2026);
        y2026.setMonths(new ArrayList<>());

        MonthlyWorkload m10 = new MonthlyWorkload();
        m10.setMonth(10);
        m10.setSummaryDuration(100);

        MonthlyWorkload m2 = new MonthlyWorkload();
        m2.setMonth(2);
        m2.setSummaryDuration(50);

        y2026.getMonths().add(m10);
        y2026.getMonths().add(m2);

        YearlyWorkload y2025 = new YearlyWorkload();
        y2025.setYear(2025);
        y2025.setMonths(new ArrayList<>());

        workload.getYears().add(y2026);
        workload.getYears().add(y2025);

        WorkloadSummaryResponse response = mapper.toResponse(workload);

        assertEquals("test.trainer", response.trainerUsername());
        assertEquals(2, response.years().size());
        assertEquals(2025, response.years().get(0).year());
        assertEquals(2026, response.years().get(1).year());
        assertEquals(2, response.years().get(1).months().get(0).month());
        assertEquals(10, response.years().get(1).months().get(1).month());
    }
}