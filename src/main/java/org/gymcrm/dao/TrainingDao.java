package org.gymcrm.dao;

import org.gymcrm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training save(Training training);

    Optional<Training> findById(Long trainingId);

    List<Training> findAll();
}
