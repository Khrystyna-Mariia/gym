package org.gymcrm.service.impl;

import org.gymcrm.actuator.GymMetrics;
import org.gymcrm.dao.TraineeDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Role;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.User;
import org.gymcrm.service.RegistrationResult;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    private TraineeServiceImpl traineeService;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UserProfileInitializer userProfileInitializer;

    @Mock
    private TrainerService trainerService;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(
                traineeDao,
                userProfileInitializer,
                trainerService,
                gymMetrics,
                passwordEncoder
        );
    }

    @Test
    void shouldCreateTraineeUsingInitializerAndDao() {
        Trainee newTrainee = createTrainee(null, "John", "Smith", null);
        newTrainee.getUser().setPassword(null);
        newTrainee.getUser().setActive(false);

        Trainee savedTrainee = createTrainee(2L, "John", "Smith", "John.Smith1");
        savedTrainee.getUser().setPassword("EncodedGenerated1");
        savedTrainee.getUser().setActive(true);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("John.Smith1");
            user.setPassword("EncodedGenerated1");
            user.setActive(true);
            return "Generated1";
        }).when(userProfileInitializer).initialize(any(User.class), eq(Role.TRAINEE));

        when(traineeDao.save(newTrainee)).thenReturn(savedTrainee);

        RegistrationResult<Trainee> result = traineeService.create(newTrainee);

        assertNotNull(result);
        assertEquals(2L, result.entity().getId());
        assertEquals("John.Smith1", result.entity().getUser().getUsername());
        assertEquals("Generated1", result.rawPassword());
        assertTrue(result.entity().getUser().isActive());

        verify(userProfileInitializer).initialize(newTrainee.getUser(), Role.TRAINEE);
        verify(traineeDao).save(newTrainee);
        verify(gymMetrics).incrementTraineeRegistrations();
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTrainee() {
        assertThrows(ValidationException.class, () -> traineeService.create(null));

        verifyNoInteractions(userProfileInitializer, traineeDao, gymMetrics);
    }

    @Test
    void shouldThrowExceptionWhenTraineeHasNullUser() {
        Trainee traineeWithNoUser = new Trainee();
        traineeWithNoUser.setUser(null);

        assertThrows(ValidationException.class, () -> traineeService.create(traineeWithNoUser));
        verifyNoInteractions(userProfileInitializer, traineeDao, gymMetrics);
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown", "Anna.Brown");

        when(traineeDao.existsByUsername("Anna.Brown")).thenReturn(true);
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.update(trainee);

        assertEquals(trainee, result);
        verify(traineeDao).existsByUsername("Anna.Brown");
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTrainee() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown", "Anna.Brown");
        when(traineeDao.existsByUsername("Anna.Brown")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> traineeService.update(trainee));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        String username = "Anna.Brown";
        Trainee trainee = createTrainee(1L, "Anna", "Brown", username);

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(username);

        verify(traineeDao).findByUsername(username);
        verify(traineeDao).deleteById(1L);
    }

    @Test
    void shouldSelectTraineeById() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown", "Anna.Brown");

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectById(1L);

        assertEquals(Optional.of(trainee), result);
        verify(traineeDao).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenTraineeNotFound() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.selectById(99L);

        assertTrue(result.isEmpty());
        verify(traineeDao).findById(99L);
    }

    @Test
    void shouldSelectAllTrainees() {
        Trainee firstTrainee = createTrainee(1L, "John", "Smith", "John.Smith");
        Trainee secondTrainee = createTrainee(2L, "Anna", "Brown", "Anna.Brown");

        when(traineeDao.findAll()).thenReturn(List.of(firstTrainee, secondTrainee));

        List<Trainee> result = traineeService.selectAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainee));
        assertTrue(result.contains(secondTrainee));
        verify(traineeDao).findAll();
    }

    @Test
    void shouldSelectTraineeByUsername() {
        String username = "john.smith";
        Trainee trainee = createTrainee(1L, "John", "Smith", username);
        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(traineeDao).findByUsername(username);
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatches() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("password123", "password123")).thenReturn(true);
        when(passwordEncoder.encode("newSecretPass")).thenReturn("encodedNewSecretPass");

        traineeService.changePassword("john.smith", "password123", "newSecretPass");

        assertEquals("encodedNewSecretPass", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldActivateTraineeAccount() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        trainee.getUser().setActive(false);
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        traineeService.activate("john.smith");

        assertTrue(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldDeactivateTraineeAccount() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        trainee.getUser().setActive(true);
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        traineeService.deactivate("john.smith");

        assertFalse(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldUpdateTrainersList() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        Trainer trainer = new Trainer();
        trainer.setId(10L);
        User trainerUser = new User(10L, "Jack", "Coach", "trainer.jack", "pass", true, Role.TRAINER);
        trainer.setUser(trainerUser);

        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));
        when(trainerService.selectByUsernames(List.of("trainer.jack"))).thenReturn(List.of(trainer));

        traineeService.updateTrainersList("john.smith", List.of("trainer.jack"));

        assertEquals(1, trainee.getTrainers().size());
        verify(traineeDao).update(trainee);
    }

    @Test
    void validateTrainee_shouldThrowWhenUpdateAndIdNull() {
        Trainee trainee = createTrainee(null, "John", "Smith", "john.smith");
        assertThrows(ValidationException.class, () -> traineeService.update(trainee));
    }

    @Test
    void validateTrainee_shouldThrowWhenFirstNameNullOrBlank() {
        Trainee nullName = createTrainee(1L, null, "Smith", "john.smith");
        assertThrows(ValidationException.class, () -> traineeService.create(nullName));

        Trainee blankName = createTrainee(1L, "   ", "Smith", "john.smith");
        assertThrows(ValidationException.class, () -> traineeService.create(blankName));
    }

    @Test
    void validateTrainee_shouldThrowWhenLastNameNullOrBlank() {
        Trainee nullLastName = createTrainee(1L, "John", null, "john.smith");
        assertThrows(ValidationException.class, () -> traineeService.create(nullLastName));
    }

    @Test
    void validateTrainee_shouldThrowWhenUpdateAndUsernameNullOrBlank() {
        Trainee nullUsername = createTrainee(1L, "John", "Smith", null);
        assertThrows(ValidationException.class, () -> traineeService.update(nullUsername));
    }

    @Test
    void deleteByUsername_shouldThrowWhenUsernameNullOrBlank() {
        assertThrows(ValidationException.class, () -> traineeService.deleteByUsername(null));
        assertThrows(ValidationException.class, () -> traineeService.deleteByUsername(""));
    }

    @Test
    void deleteByUsername_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.deleteByUsername("ghost"));
    }

    @Test
    void changePassword_shouldThrowWhenNewPasswordNullOrBlank() {
        assertThrows(ValidationException.class, () -> traineeService.changePassword("user", "old", null));
        assertThrows(ValidationException.class, () -> traineeService.changePassword("user", "old", "  "));
    }

    @Test
    void changePassword_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.changePassword("ghost", "old", "new"));
    }

    @Test
    void changePassword_shouldThrowWhenOldPasswordMismatches() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("wrong_old_pass", "password123")).thenReturn(false);

        assertThrows(ValidationException.class, () -> traineeService.changePassword("john.smith", "wrong_old_pass", "newPass"));
    }

    @Test
    void activate_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.activate("ghost"));
    }

    @Test
    void activate_shouldThrowWhenAlreadyActive() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        trainee.getUser().setActive(true);
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class, () -> traineeService.activate("john.smith"));
    }

    @Test
    void deactivate_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.deactivate("ghost"));
    }

    @Test
    void deactivate_shouldThrowWhenAlreadyInactive() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        trainee.getUser().setActive(false);
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class, () -> traineeService.deactivate("john.smith"));
    }

    @Test
    void updateTrainersList_shouldThrowWhenArgsNull() {
        assertThrows(ValidationException.class, () -> traineeService.updateTrainersList(null, List.of("trainer")));
        assertThrows(ValidationException.class, () -> traineeService.updateTrainersList("user", null));
    }

    @Test
    void updateTrainersList_shouldThrowWhenTraineeNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.updateTrainersList("ghost", List.of("trainer")));
    }

    @Test
    void updateTrainersList_shouldThrowWhenSomeTrainerNotFound() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        Trainer existingTrainer = new Trainer();
        existingTrainer.setUser(new User(10L, "Jack", "Coach", "trainer.jack", "pass", true, Role.TRAINER));

        List<String> requestedUsernames = List.of("trainer.jack", "missing.trainer");
        when(trainerService.selectByUsernames(requestedUsernames)).thenReturn(List.of(existingTrainer));

        assertThrows(EntityNotFoundException.class, () ->
                traineeService.updateTrainersList("john.smith", requestedUsernames)
        );
    }

    private Trainee createTrainee(Long id, String firstName, String lastName, String username) {
        User user = new User(id, firstName, lastName, username, "password123", true, Role.TRAINEE);
        Trainee trainee = new Trainee();
        trainee.setId(id);
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Kyiv");
        return trainee;
    }
}