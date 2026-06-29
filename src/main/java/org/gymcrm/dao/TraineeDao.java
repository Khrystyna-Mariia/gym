package org.gymcrm.dao;

import org.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);

    Trainee update(Trainee trainee);

    boolean  deleteById(Long userId);

    Optional<Trainee> findById(Long userId);

    List<Trainee> findAll();

    boolean existsByUsername(String username);
}
