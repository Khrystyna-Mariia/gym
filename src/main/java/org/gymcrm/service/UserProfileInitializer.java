package org.gymcrm.service;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.User;
import org.gymcrm.util.PasswordGenerator;
import org.gymcrm.util.UsernameGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserProfileInitializer {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    public UserProfileInitializer(
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            PasswordEncoder passwordEncoder
    ) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public String initialize(User user) {
        if (user == null) {
            throw new ValidationException("User must not be null");
        }

        String username = usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                usernameToCheck -> traineeDao.existsByUsername(usernameToCheck)
                        || trainerDao.existsByUsername(usernameToCheck)
        );

        String rawPassword = passwordGenerator.generate();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        return rawPassword;
    }
}