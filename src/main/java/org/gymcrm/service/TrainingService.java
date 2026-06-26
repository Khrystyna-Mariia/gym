package org.gymcrm.service;

import org.gymcrm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    Optional<Training> selectById(Long id);

    List<Training> selectAll();
}
