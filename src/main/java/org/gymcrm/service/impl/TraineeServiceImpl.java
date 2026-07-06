package org.gymcrm.service.impl;

import org.gymcrm.annotation.RequireAuth;
import org.gymcrm.dao.TraineeDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.User;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final UserProfileInitializer userProfileInitializer;
    private final TrainerService trainerService;

    public TraineeServiceImpl(TraineeDao traineeDao, UserProfileInitializer userProfileInitializer, TrainerService trainerService) {
        this.traineeDao = traineeDao;
        this.userProfileInitializer = userProfileInitializer;
        this.trainerService = trainerService;
    }

    @Override
    public Trainee create(Trainee trainee) {
        validateTrainee(trainee, false);
        userProfileInitializer.initialize(trainee.getUser());
        return traineeDao.save(trainee);
    }

    @Override
    @RequireAuth
    public Trainee update(Trainee trainee) {
        validateTrainee(trainee, true);
        if (!traineeDao.existsByUsername(trainee.getUser().getUsername())) {
            throw new EntityNotFoundException("Cannot update: Trainee profile does not exist");
        }
        return traineeDao.update(trainee);
    }

    @Override
    @RequireAuth
    public void deleteByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username must not be empty");
        }

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username " + username + " not found"));

        traineeDao.deleteById(trainee.getId());
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public Optional<Trainee> selectById(Long id) {
        return traineeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Trainee> selectAll() {
        return traineeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public Optional<Trainee> selectByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        return traineeDao.findByUsername(username)
                .map(Trainee::getUser)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    @RequireAuth
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password cannot be empty");
        }

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username " + username + " not found"));

        User user = trainee.getUser();
        if (!user.getPassword().equals(oldPassword)) {
            throw new ValidationException("Invalid old password provided");
        }

        user.setPassword(newPassword);
        traineeDao.update(trainee);
        logger.info("Password successfully changed for user: {}", username);
    }

    @Override
    @RequireAuth
    public void activate(String username) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username " + username + " not found"));

        User user = trainee.getUser();
        if (user.isActive()) {
            throw new ValidationException("Trainee account is already active");
        }

        user.setActive(true);
        traineeDao.update(trainee);
        logger.info("Trainee account activated: {}", username);
    }

    @Override
    @RequireAuth
    public void deactivate(String username) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username " + username + " not found"));

        User user = trainee.getUser();
        if (!user.isActive()) {
            throw new ValidationException("Trainee account is already inactive");
        }

        user.setActive(false);
        traineeDao.update(trainee);
        logger.info("Trainee account deactivated: {}", username);
    }

    @Override
    @RequireAuth
    public void updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        if (traineeUsername == null || trainerUsernames == null) {
            throw new ValidationException("Trainee username and trainer usernames list must not be null");
        }
        logger.info("Updating trainers list for trainee: {}", traineeUsername);

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username " + traineeUsername + " not found"));

        List<Trainer> trainers = trainerService.selectByUsernames(trainerUsernames);

        if (trainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(t -> t.getUser().getUsername().toLowerCase())
                    .collect(toSet());

            for (String tUsername : trainerUsernames) {
                if (!foundUsernames.contains(tUsername.toLowerCase())) {
                    throw new EntityNotFoundException("Trainer with username " + tUsername + " not found");
                }
            }
        }

        trainee.setTrainers(new java.util.HashSet<>(trainers));
        traineeDao.update(trainee);
        logger.info("Trainers list successfully updated for trainee: {}", traineeUsername);
    }

    private void validateTrainee(Trainee trainee, boolean isUpdate) {
        if (trainee == null) throw new ValidationException("Trainee data must not be null");
        if (isUpdate && trainee.getId() == null) throw new ValidationException("Trainee ID is required for update");

        User user = trainee.getUser();
        if (user == null) throw new ValidationException("Associated User data must not be null");
        if (user.getFirstName() == null || user.getFirstName().isBlank()) throw new ValidationException("First name is required");
        if (user.getLastName() == null || user.getLastName().isBlank()) throw new ValidationException("Last name is required");
        if (isUpdate && (user.getUsername() == null || user.getUsername().isBlank())) {
            throw new ValidationException("Username is required for update");
        }
    }
}