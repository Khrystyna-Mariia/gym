package org.gymcrm.service;

import org.gymcrm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    List<TrainingType> selectAll();
    Optional<TrainingType> selectById(Long id);
}