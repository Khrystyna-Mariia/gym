package org.gymcrm.service;

import org.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Trainee update(Trainee trainee);

    void deleteByUsername(String username);

    Optional<Trainee> selectById(Long userId);

    List<Trainee> selectAll();

    Optional<Trainee> selectByUsername(String username);

    boolean authenticate(String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);

    void activate(String username);

    void deactivate(String username);

    void updateTrainersList(String traineeUsername, List<String> trainerUsernames);
}