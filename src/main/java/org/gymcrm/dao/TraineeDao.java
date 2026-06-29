package org.gymcrm.dao;

import org.gymcrm.model.Trainee;

public interface TraineeDao extends CrudDao<Trainee, Long> {
    Trainee update(Trainee trainee);

    boolean deleteById(Long userId);

    boolean existsByUsername(String username);
}