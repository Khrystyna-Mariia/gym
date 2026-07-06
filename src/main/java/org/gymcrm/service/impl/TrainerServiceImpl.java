package org.gymcrm.service.impl;

import org.gymcrm.annotation.RequireAuth;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.User;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final UserProfileInitializer userProfileInitializer;

    public TrainerServiceImpl(TrainerDao trainerDao, UserProfileInitializer userProfileInitializer) {
        this.trainerDao = trainerDao;
        this.userProfileInitializer = userProfileInitializer;
    }

    @Override
    public Trainer create(Trainer trainer) {
        validateTrainer(trainer, false);
        userProfileInitializer.initialize(trainer.getUser());
        return trainerDao.save(trainer);
    }

    @Override
    @RequireAuth
    public Trainer update(Trainer trainer) {
        validateTrainer(trainer, true);
        if (!trainerDao.existsByUsername(trainer.getUser().getUsername())) {
            throw new EntityNotFoundException("Cannot update: Trainer profile does not exist");
        }
        return trainerDao.update(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public Optional<Trainer> selectById(Long id) {
        return trainerDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Trainer> selectAll() {
        return trainerDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public Optional<Trainer> selectByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username must not be empty");
        }
        return trainerDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new ValidationException("Trainee username must not be empty");
        }
        return trainerDao.findTrainersNotAssignedToTrainee(traineeUsername);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireAuth
    public List<Trainer> selectByUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return emptyList();
        }
        return trainerDao.findByUsernames(usernames);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        return trainerDao.findByUsername(username)
                .map(Trainer::getUser)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    @RequireAuth
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password cannot be empty");
        }

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username " + username + " not found"));

        User user = trainer.getUser();
        if (!user.getPassword().equals(oldPassword)) {
            throw new ValidationException("Invalid old password provided");
        }

        user.setPassword(newPassword);
        trainerDao.update(trainer);
        logger.info("Password successfully changed for trainer: {}", username);
    }

    @Override
    @RequireAuth
    public void activate(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username " + username + " not found"));

        User user = trainer.getUser();
        if (user.isActive()) {
            throw new ValidationException("Trainer account is already active");
        }

        user.setActive(true);
        trainerDao.update(trainer);
        logger.info("Trainer account activated: {}", username);
    }

    @Override
    @RequireAuth
    public void deactivate(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username " + username + " not found"));

        User user = trainer.getUser();

        if (!user.isActive()) {
            throw new ValidationException("Trainer account is already inactive");
        }

        user.setActive(false);
        trainerDao.update(trainer);
        logger.info("Trainer account deactivated: {}", username);
    }

    private void validateTrainer(Trainer trainer, boolean isUpdate) {
        if (trainer == null) throw new ValidationException("Trainer data must not be null");
        if (isUpdate && trainer.getId() == null) throw new ValidationException("Trainer ID is required for update");

        User user = trainer.getUser();
        if (user == null) throw new ValidationException("Associated User data must not be null");
        if (user.getFirstName() == null || user.getFirstName().isBlank()) throw new ValidationException("First name is required");
        if (user.getLastName() == null || user.getLastName().isBlank()) throw new ValidationException("Last name is required");
        if (trainer.getSpecialization() == null) throw new ValidationException("Specialization is required");
        if (isUpdate && (user.getUsername() == null || user.getUsername().isBlank())) {
            throw new ValidationException("Username is required for update");
        }
    }
}