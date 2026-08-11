package org.gymcrm.workload.dto;

import java.util.List;

public record WorkloadSummaryResponse(
        String trainerUsername,
        String firstName,
        String lastName,
        boolean isActive,
        List<YearSummary> years
) {
    public record YearSummary(int year, List<MonthSummary> months) {}
    public record MonthSummary(int month, int summaryDuration) {}
}