package org.gymcrm.dao;

import org.gymcrm.model.Trainer;

public interface TrainerDao extends CrudDao<Trainer, Long> {
    Trainer update(Trainer trainer);

    boolean existsByUsername(String username);
}