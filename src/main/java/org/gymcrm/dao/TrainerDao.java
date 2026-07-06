package org.gymcrm.dao;

import org.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao extends CrudDao<Trainer, Long> {
    Trainer update(Trainer trainer);

    boolean existsByUsername(String username);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername);

    List<Trainer> findByUsernames(List<String> usernames);
}