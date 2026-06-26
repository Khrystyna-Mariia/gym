package org.gymcrm.service;

import org.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> selectById(Long userId);

    List<Trainer> selectAll();
}
