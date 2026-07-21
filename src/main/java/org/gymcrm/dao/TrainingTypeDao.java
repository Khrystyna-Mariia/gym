package org.gymcrm.dao;

import org.gymcrm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    List<TrainingType> findAll();
    Optional<TrainingType> findById(Long id);
}