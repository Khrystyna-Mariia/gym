package org.gymcrm.service;

import org.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Trainee update(Trainee trainee);

    void delete(Long userId);

    Optional<Trainee> selectById(Long userId);

    List<Trainee> selectAll();

    Optional<Trainee> selectByUsername(String username);
}