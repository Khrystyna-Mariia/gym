package org.gymcrm.service.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.User;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.UserProfileInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(traineeDao, userProfileInitializer, trainerService);
    }

    @Test
    void shouldCreateTraineeUsingInitializerAndDao() {
        Trainee newTrainee = createTrainee(null, "John", "Smith", null);
        newTrainee.getUser().setPassword(null);
        newTrainee.getUser().setActive(false);

        Trainee savedTrainee = createTrainee(2L, "John", "Smith", "John.Smith1");
        savedTrainee.getUser().setPassword("Generated1");
        savedTrainee.getUser().setActive(true);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("John.Smith1");
            user.setPassword("Generated1");
            user.setActive(true);
            return null;
        }).when(userProfileInitializer).initialize(any(User.class));

        when(traineeDao.save(newTrainee)).thenReturn(savedTrainee);

        Trainee result = traineeService.create(newTrainee);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("John.Smith1", result.getUser().getUsername());
        assertEquals("Generated1", result.getUser().getPassword());
        assertTrue(result.getUser().isActive());

        verify(userProfileInitializer).initialize(newTrainee.getUser());
        verify(traineeDao).save(newTrainee);
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTrainee() {
        assertThrows(ValidationException.class, () -> traineeService.create(null));

        verifyNoInteractions(userProfileInitializer, traineeDao);
    }

    @Test
    void shouldThrowExceptionWhenTraineeHasNullUser() {
        Trainee traineeWithNoUser = new Trainee();
        traineeWithNoUser.setUser(null);

        assertThrows(ValidationException.class, () -> traineeService.create(traineeWithNoUser));
        verifyNoInteractions(userProfileInitializer, traineeDao);
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
    void shouldAuthenticateSuccessfully() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        boolean authenticated = traineeService.authenticate("john.smith", "password123");

        assertTrue(authenticated);
    }

    @Test
    void shouldFailAuthenticationWithWrongPassword() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        boolean authenticated = traineeService.authenticate("john.smith", "wrong_pass");

        assertFalse(authenticated);
    }

    @Test
    void shouldChangePasswordWhenOldPasswordMatches() {
        Trainee trainee = createTrainee(1L, "John", "Smith", "john.smith");
        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("john.smith", "password123", "newSecretPass");

        assertEquals("newSecretPass", trainee.getUser().getPassword());
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
        User trainerUser = new User(10L, "Jack", "Coach", "trainer.jack", "pass", true);
        trainer.setUser(trainerUser);

        when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));

        when(trainerService.selectByUsernames(List.of("trainer.jack"))).thenReturn(List.of(trainer));

        traineeService.updateTrainersList("john.smith", List.of("trainer.jack"));

        assertEquals(1, trainee.getTrainers().size());
        verify(traineeDao).update(trainee);
    }

    private Trainee createTrainee(Long id, String firstName, String lastName, String username) {
        User user = new User(id, firstName, lastName, username, "password123", true);
        Trainee trainee = new Trainee();
        trainee.setId(id);
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Kyiv");
        return trainee;
    }
}