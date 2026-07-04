package org.gymcrm.service;

import org.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    Optional<Training> selectById(Long id);

    List<Training> selectAll();

    List<Training> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String typeName);

    List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName);

    }
