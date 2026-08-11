package org.gymcrm.workload.service;

import org.gymcrm.workload.dto.WorkloadRequest;
import org.gymcrm.workload.dto.WorkloadSummaryResponse;
import org.gymcrm.workload.mapper.WorkloadMapper;
import org.gymcrm.workload.model.MonthlyWorkload;
import org.gymcrm.workload.model.TrainerWorkload;
import org.gymcrm.workload.model.YearlyWorkload;
import org.gymcrm.workload.repository.TrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TrainerWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadService.class);

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadMapper workloadMapper;

    public TrainerWorkloadService(TrainerWorkloadRepository trainerWorkloadRepository, WorkloadMapper workloadMapper) {
        this.workloadMapper = workloadMapper;
        this.trainerWorkloadRepository = trainerWorkloadRepository;
    }

    public void processWorkload(WorkloadRequest request) {
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findByTrainerUsername(request.trainerUsername())
                .orElseGet(() -> createTrainerWorkload(request));

        trainerWorkload.setFirstName(request.trainerFirstName());
        trainerWorkload.setLastName(request.trainerLastName());
        trainerWorkload.setActive(request.isActive());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        YearlyWorkload yearlyWorkload = findOrCreateYear(trainerWorkload, year);
        MonthlyWorkload monthlyWorkload = findOrCreateMonth(yearlyWorkload, month);

        applyAction(monthlyWorkload, request);

        trainerWorkloadRepository.save(trainerWorkload);
        logger.info("Processed {} of {} minutes for trainer '{}' on {}-{}: new total = {}",
                request.actionType(), request.trainingDuration(), request.trainerUsername(),
                year, month, monthlyWorkload.getSummaryDuration());
    }

    public Optional<WorkloadSummaryResponse> getSummary(String trainerUsername) {
        return trainerWorkloadRepository.findByTrainerUsername(trainerUsername)
                .map(workloadMapper::toResponse);
    }

    private TrainerWorkload createTrainerWorkload(WorkloadRequest request) {
        TrainerWorkload trainerWorkload = new TrainerWorkload();
        trainerWorkload.setTrainerUsername(request.trainerUsername());
        return trainerWorkload;
    }

    private YearlyWorkload findOrCreateYear(TrainerWorkload trainerWorkload, int year) {
        return trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearlyWorkload newYear = new YearlyWorkload();
                    newYear.setTrainerWorkload(trainerWorkload);
                    newYear.setYear(year);
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });
    }

    private MonthlyWorkload findOrCreateMonth(YearlyWorkload yearlyWorkload, int month) {
        return yearlyWorkload.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthlyWorkload newMonth = new MonthlyWorkload();
                    newMonth.setYearlyWorkload(yearlyWorkload);
                    newMonth.setMonth(month);
                    newMonth.setSummaryDuration(0);
                    yearlyWorkload.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private void applyAction(MonthlyWorkload monthlyWorkload, WorkloadRequest request) {
        int delta = switch (request.actionType()) {
            case ADD -> request.trainingDuration();
            case DELETE -> -request.trainingDuration();
        };
        int updated = monthlyWorkload.getSummaryDuration() + delta;
        monthlyWorkload.setSummaryDuration(Math.max(updated, 0));

        if (updated < 0) {
            logger.warn("Workload for trainer went negative ({} min), clamped to 0 — check for duplicate/out-of-order DELETE events", updated);
        }
    }
}