package org.gymcrm.dao;

import org.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> findById(Long userId);

    List<Trainer> findAll();

    boolean existsByUsername(String username);
}
