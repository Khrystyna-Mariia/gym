package org.gymcrm.dao;

import org.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingDao extends CrudDao<Training, Long> {
    List<Training> findTraineeTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName);

    List<Training> findTrainerTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName);
}