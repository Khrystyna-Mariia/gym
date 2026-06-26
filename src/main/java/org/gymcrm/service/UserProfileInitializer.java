package org.gymcrm.service;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.User;
import org.gymcrm.util.PasswordGenerator;
import org.gymcrm.util.UsernameGenerator;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class UserProfileInitializer {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public UserProfileInitializer(
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator
    ) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public void initialize(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        String username = usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                getAllExistingUsernames()
        );

        user.setUsername(username);
        user.setPassword(passwordGenerator.generate());
        user.setActive(true);
    }

    private Set<String> getAllExistingUsernames() {
        Set<String> usernames = new HashSet<>();

        traineeDao.findAll()
                .stream()
                .map(Trainee::getUsername)
                .filter(Objects::nonNull)
                .forEach(usernames::add);

        trainerDao.findAll()
                .stream()
                .map(Trainer::getUsername)
                .filter(Objects::nonNull)
                .forEach(usernames::add);

        return usernames;
    }
}