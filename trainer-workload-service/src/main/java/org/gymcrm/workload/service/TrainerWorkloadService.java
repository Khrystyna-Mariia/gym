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

import java.util.List;
import java.util.Optional;

@Service
public class TrainerWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadService.class);

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadMapper workloadMapper;

    public TrainerWorkloadService(TrainerWorkloadRepository trainerWorkloadRepository, WorkloadMapper workloadMapper) {
        this.workloadMapper = workloadMapper;
        this.trainerWorkloadRepository = trainerWorkloadRepository;
    }

    public void processWorkload(WorkloadRequest event) {
        logger.info("Processing workload event: trainer={}, action={}, duration={}",
                event.trainerUsername(), event.actionType(), event.trainingDuration());

        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findByTrainerUsername(event.trainerUsername())
                .map(existing -> {
                    logger.debug("Operation: found existing trainer record for '{}'", event.trainerUsername());
                    return existing;
                })
                .orElseGet(() -> {
                    logger.debug("Operation: no trainer record found for '{}', creating a new one", event.trainerUsername());
                    return createTrainerWorkload(event);
                });

        trainerWorkload.setFirstName(event.trainerFirstName());
        trainerWorkload.setLastName(event.trainerLastName());
        trainerWorkload.setActive(event.isActive());

        int year = event.trainingDate().getYear();
        int month = event.trainingDate().getMonthValue();

        YearlyWorkload yearlyWorkload = findOrCreateYear(trainerWorkload, year);
        MonthlyWorkload monthlyWorkload = findOrCreateMonth(yearlyWorkload, month);

        applyAction(monthlyWorkload, event);

        trainerWorkloadRepository.save(trainerWorkload);
        logger.info("Processed {} of {} minutes for trainer '{}' on {}-{}: new total = {}",
                event.actionType(), event.trainingDuration(), event.trainerUsername(),
                year, month, monthlyWorkload.getSummaryDuration());
    }

    public Optional<WorkloadSummaryResponse> getSummary(String trainerUsername) {
        return trainerWorkloadRepository.findByTrainerUsername(trainerUsername)
                .map(workloadMapper::toResponse);
    }

    public List<WorkloadSummaryResponse> search(String firstName, String lastName) {
        return trainerWorkloadRepository.findByFirstNameAndLastName(firstName, lastName).stream()
                .map(workloadMapper::toResponse)
                .toList();
    }

    private TrainerWorkload createTrainerWorkload(WorkloadRequest event) {
        TrainerWorkload trainerWorkload = new TrainerWorkload();
        trainerWorkload.setTrainerUsername(event.trainerUsername());
        return trainerWorkload;
    }

    private YearlyWorkload findOrCreateYear(TrainerWorkload trainerWorkload, int year) {
        return trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Operation: creating new Year element {} for trainer '{}'", year, trainerWorkload.getTrainerUsername());
                    YearlyWorkload newYear = new YearlyWorkload();
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
                    logger.debug("Operation: creating new Month element {} for year {}", month, yearlyWorkload.getYear());
                    MonthlyWorkload newMonth = new MonthlyWorkload();
                    newMonth.setMonth(month);
                    newMonth.setSummaryDuration(0);
                    yearlyWorkload.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private void applyAction(MonthlyWorkload monthlyWorkload, WorkloadRequest event) {
        int delta = switch (event.actionType()) {
            case ADD -> event.trainingDuration();
            case DELETE -> -event.trainingDuration();
        };
        int updated = monthlyWorkload.getSummaryDuration() + delta;
        logger.debug("Operation: updating Trainings summary duration from {} to {} ({} {})",
                monthlyWorkload.getSummaryDuration(), Math.max(updated, 0), event.actionType(), delta);
        monthlyWorkload.setSummaryDuration(Math.max(updated, 0));

        if (updated < 0) {
            logger.warn("Workload for trainer went negative ({} min), clamped to 0 — check for duplicate/out-of-order DELETE events", updated);
        }
    }
}