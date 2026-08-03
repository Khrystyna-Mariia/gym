package org.gymcrm.service.impl;

import org.gymcrm.actuator.GymMetrics;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Role;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.model.User;
import org.gymcrm.service.RegistrationResult;
import org.gymcrm.service.UserProfileInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private TrainerServiceImpl trainerService;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserProfileInitializer userProfileInitializer;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(
                trainerDao,
                userProfileInitializer,
                gymMetrics,
                passwordEncoder
        );
    }

    @Test
    void shouldCreateTrainerUsingInitializerAndDao() {
        Trainer newTrainer = createTrainer(null, "John", "Smith", null);
        newTrainer.getUser().setPassword(null);
        newTrainer.getUser().setActive(false);

        Trainer savedTrainer = createTrainer(2L, "John", "Smith", "John.Smith1");
        savedTrainer.getUser().setPassword("EncodedGenerated1");
        savedTrainer.getUser().setActive(true);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("John.Smith1");
            user.setPassword("EncodedGenerated1");
            user.setActive(true);
            return "Generated1";
        }).when(userProfileInitializer).initialize(any(User.class), eq(Role.TRAINER));

        when(trainerDao.save(newTrainer)).thenReturn(savedTrainer);

        RegistrationResult<Trainer> result = trainerService.create(newTrainer);

        assertNotNull(result);
        assertEquals(2L, result.entity().getId());
        assertEquals("John.Smith1", result.entity().getUser().getUsername());
        assertEquals("Generated1", result.rawPassword());
        assertTrue(result.entity().getUser().isActive());

        verify(userProfileInitializer).initialize(newTrainer.getUser(), Role.TRAINER);
        verify(trainerDao).save(newTrainer);
        verify(gymMetrics).incrementTrainerRegistrations();
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTrainer() {
        assertThrows(ValidationException.class, () -> trainerService.create(null));

        verifyNoInteractions(userProfileInitializer, trainerDao, gymMetrics);
    }

    @Test
    void shouldThrowExceptionWhenTrainerHasNullUser() {
        Trainer trainerWithNoUser = new Trainer();
        trainerWithNoUser.setUser(null);

        assertThrows(ValidationException.class, () -> trainerService.create(trainerWithNoUser));
        verifyNoInteractions(userProfileInitializer, trainerDao, gymMetrics);
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        when(trainerDao.existsByUsername("Michael.Green")).thenReturn(true);
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer result = trainerService.update(trainer);

        assertEquals(trainer, result);
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTrainer() {
        Trainer trainer = createTrainer(1L, "Michael", "Green", "NonExistent.User");
        when(trainerDao.existsByUsername("NonExistent.User")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> trainerService.update(trainer));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void shouldSelectTrainerById() {
        Trainer trainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.selectById(1L);

        assertEquals(Optional.of(trainer), result);
        verify(trainerDao).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainerNotFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.selectById(99L);

        assertTrue(result.isEmpty());
        verify(trainerDao).findById(99L);
    }

    @Test
    void shouldSelectAllTrainers() {
        Trainer firstTrainer = createTrainer(1L, "Michael", "Green", "Michael.Green");
        Trainer secondTrainer = createTrainer(2L, "Olivia", "White", "Olivia.White");

        when(trainerDao.findAll()).thenReturn(List.of(firstTrainer, secondTrainer));

        List<Trainer> result = trainerService.selectAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainer));
        assertTrue(result.contains(secondTrainer));
        verify(trainerDao).findAll();
    }

    @Test
    void shouldSelectTrainerByUsername() {
        String username = "michael.green";
        Trainer trainer = createTrainer(1L, "Michael", "Green", username);
        when(trainerDao.findByUsername(username)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.selectByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void shouldThrowExceptionWhenSelectByUsernameIsEmpty() {
        assertThrows(ValidationException.class, () -> trainerService.selectByUsername("  "));
    }

    @Test
    void shouldGetUnassignedTrainers() {
        String traineeUsername = "john.smith";
        List<Trainer> unassigned = List.of(createTrainer(5L, "Coach", "Alex", "Coach.Alex"));
        when(trainerDao.findTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(unassigned);

        List<Trainer> result = trainerService.getUnassignedTrainers(traineeUsername);

        assertEquals(1, result.size());
        verify(trainerDao).findTrainersNotAssignedToTrainee(traineeUsername);
    }

    @Test
    void shouldThrowExceptionWhenGetUnassignedTrainersUsernameIsEmpty() {
        assertThrows(ValidationException.class, () -> trainerService.getUnassignedTrainers(""));
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        Trainer trainer = createTrainer(1L, "Alex", "Brown", "alex.brown");
        when(trainerDao.findByUsername("alex.brown")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("password123", "password123")).thenReturn(true);
        when(passwordEncoder.encode("newPassword777")).thenReturn("encodedNewPassword777");

        trainerService.changePassword("alex.brown", "password123", "newPassword777");

        assertEquals("encodedNewPassword777", trainer.getUser().getPassword());
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldActivateTrainerSuccessfully() {
        Trainer trainer = createTrainer(1L, "Alex", "Brown", "alex.brown");
        trainer.getUser().setActive(false);
        when(trainerDao.findByUsername("alex.brown")).thenReturn(Optional.of(trainer));

        trainerService.activate("alex.brown");

        assertTrue(trainer.getUser().isActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldThrowExceptionWhenTrainerAlreadyActive() {
        Trainer trainer = createTrainer(1L, "Alex", "Brown", "alex.brown");
        trainer.getUser().setActive(true);
        when(trainerDao.findByUsername("alex.brown")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class, () -> trainerService.activate("alex.brown"));
    }

    @Test
    void shouldDeactivateTrainerSuccessfully() {
        Trainer trainer = createTrainer(1L, "Alex", "Brown", "alex.brown");
        trainer.getUser().setActive(true);
        when(trainerDao.findByUsername("alex.brown")).thenReturn(Optional.of(trainer));

        trainerService.deactivate("alex.brown");

        assertFalse(trainer.getUser().isActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldReturnEmptyListWhenUsernamesNullOrEmpty() {
        assertTrue(trainerService.selectByUsernames(null).isEmpty());
        assertTrue(trainerService.selectByUsernames(List.of()).isEmpty());
        verifyNoInteractions(trainerDao);
    }

    @Test
    void shouldSelectTrainersByListOfUsernames() {
        List<String> usernames = List.of("user1", "user2");
        when(trainerDao.findByUsernames(usernames)).thenReturn(List.of(new Trainer()));

        List<Trainer> result = trainerService.selectByUsernames(usernames);

        assertFalse(result.isEmpty());
        verify(trainerDao).findByUsernames(usernames);
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsEmpty() {
        assertThrows(ValidationException.class, () ->
                trainerService.changePassword("user", "old", "  "));
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFoundOnChangePassword() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                trainerService.changePassword("unknown", "old", "new"));
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordIsInvalid() {
        Trainer trainer = createTrainer(1L, "A", "B", "user");
        when(trainerDao.findByUsername("user")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("wrong_old", "password123")).thenReturn(false);

        assertThrows(ValidationException.class, () ->
                trainerService.changePassword("user", "wrong_old", "newPass"));
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFoundOnActivateOrDeactivate() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.activate("unknown"));
        assertThrows(EntityNotFoundException.class, () -> trainerService.deactivate("unknown"));
    }

    @Test
    void shouldThrowExceptionWhenTrainerAlreadyInactiveOnDeactivate() {
        Trainer trainer = createTrainer(1L, "A", "B", "user");
        trainer.getUser().setActive(false);
        when(trainerDao.findByUsername("user")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class, () -> trainerService.deactivate("user"));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTrainerWithNullId() {
        Trainer trainer = createTrainer(null, "John", "Doe", "john.doe");
        assertThrows(ValidationException.class, () -> trainerService.update(trainer));
    }

    @Test
    void shouldThrowExceptionWhenFirstNameOrLastNameMissing() {
        Trainer t1 = createTrainer(1L, "  ", "Doe", "user");
        Trainer t2 = createTrainer(1L, "John", null, "user");

        assertThrows(ValidationException.class, () -> trainerService.create(t1));
        assertThrows(ValidationException.class, () -> trainerService.create(t2));
    }

    @Test
    void shouldThrowExceptionWhenSpecializationIsNull() {
        Trainer trainer = createTrainer(1L, "John", "Doe", "user");
        trainer.setSpecialization(null);

        assertThrows(ValidationException.class, () -> trainerService.create(trainer));
    }

    @Test
    void shouldThrowExceptionWhenUsernameMissingOnUpdate() {
        Trainer trainer = createTrainer(1L, "John", "Doe", "  ");
        assertThrows(ValidationException.class, () -> trainerService.update(trainer));
    }

    @Test
    void shouldThrowExceptionWhenUsernameOrTraineeUsernameNullOrBlank() {
        assertThrows(ValidationException.class, () -> trainerService.selectByUsername(null));
        assertThrows(ValidationException.class, () -> trainerService.getUnassignedTrainers(null));
    }

    private Trainer createTrainer(Long id, String firstName, String lastName, String username) {
        User user = new User(id, firstName, lastName, username, "password123", true, Role.TRAINER);
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.FITNESS));
        return trainer;
    }
}