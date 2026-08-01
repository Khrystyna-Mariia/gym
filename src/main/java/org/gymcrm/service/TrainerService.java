package org.gymcrm.service;

import org.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    RegistrationResult<Trainer> create(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> selectById(Long userId);

    List<Trainer> selectAll();

    Optional<Trainer> selectByUsername(String username);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    List<Trainer> selectByUsernames(List<String> usernames);

    void changePassword(String username, String oldPassword, String newPassword);

    void activate(String username);

    void deactivate(String username);
}
