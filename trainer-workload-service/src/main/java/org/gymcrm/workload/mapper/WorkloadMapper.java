package org.gymcrm.workload.mapper;

import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.model.MonthlyWorkload;
import org.gymcrm.workload.model.TrainerWorkload;
import org.gymcrm.workload.model.YearlyWorkload;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkloadMapper {
    public WorkloadSummaryResponse toResponse(TrainerWorkload trainerWorkload) {
        List<WorkloadSummaryResponse.YearSummary> years = trainerWorkload.getYears().stream()
                .sorted(Comparator.comparingInt(YearlyWorkload::getYear))
                .map(y -> new WorkloadSummaryResponse.YearSummary(
                        y.getYear(),
                        y.getMonths().stream()
                                .sorted(Comparator.comparingInt(MonthlyWorkload::getMonth))
                                .map(m -> new WorkloadSummaryResponse.MonthSummary(m.getMonth(), m.getSummaryDuration()))
                                .toList()
                ))
                .toList();

        return new WorkloadSummaryResponse(
                trainerWorkload.getTrainerUsername(),
                trainerWorkload.getFirstName(),
                trainerWorkload.getLastName(),
                Boolean.TRUE.equals(trainerWorkload.getActive()),
                years
        );
    }
}