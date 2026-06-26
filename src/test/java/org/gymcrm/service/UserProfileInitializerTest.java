package org.gymcrm.service;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.util.PasswordGenerator;
import org.gymcrm.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileInitializerTest {
    private UserProfileInitializer userProfileInitializer;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        userProfileInitializer = new UserProfileInitializer(
                traineeDao,
                trainerDao,
                usernameGenerator,
                passwordGenerator
        );
    }

    @Test
    void shouldInitializeTraineeProfileWithUsernamePasswordAndActiveStatus() {
        Trainee existingTrainee = createTrainee(1L, "John", "Smith", "John.Smith");
        Trainee traineeWithoutUsername = createTrainee(2L, "Anna", "Brown", null);
        Trainer existingTrainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        Trainee newTrainee = createTrainee(null, "John", "Smith", null);
        newTrainee.setPassword(null);
        newTrainee.setActive(false);

        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee, traineeWithoutUsername));
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(usernameGenerator.generate(eq("John"), eq("Smith"), anySet())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainee);

        assertEquals("John.Smith1", newTrainee.getUsername());
        assertEquals("Generated1", newTrainee.getPassword());
        assertTrue(newTrainee.isActive());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), anySet());
        verify(passwordGenerator).generate();
    }

    @Test
    void shouldInitializeTrainerProfileWithUsernamePasswordAndActiveStatus() {
        Trainee existingTrainee = createTrainee(1L, "John", "Smith", "John.Smith");
        Trainer existingTrainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        Trainer newTrainer = createTrainer(null, "John", "Smith", null);
        newTrainer.setPassword(null);
        newTrainer.setActive(false);

        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(usernameGenerator.generate(eq("John"), eq("Smith"), anySet())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainer);

        assertEquals("John.Smith1", newTrainer.getUsername());
        assertEquals("Generated1", newTrainer.getPassword());
        assertTrue(newTrainer.isActive());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), anySet());
        verify(passwordGenerator).generate();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPassAllExistingUsernamesToUsernameGenerator() {
        Trainee existingTrainee = createTrainee(1L, "John", "Smith", "John.Smith");
        Trainee traineeWithoutUsername = createTrainee(2L, "Anna", "Brown", null);
        Trainer existingTrainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        Trainee newTrainee = createTrainee(null, "Olivia", "White", null);

        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee, traineeWithoutUsername));
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(usernameGenerator.generate(eq("Olivia"), eq("White"), anySet())).thenReturn("Olivia.White");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainee);

        ArgumentCaptor<Set<String>> usernamesCaptor = ArgumentCaptor.forClass(Set.class);

        verify(usernameGenerator).generate(eq("Olivia"), eq("White"), usernamesCaptor.capture());

        Set<String> capturedUsernames = usernamesCaptor.getValue();

        assertTrue(capturedUsernames.contains("John.Smith"));
        assertTrue(capturedUsernames.contains("Michael.Green"));
        assertFalse(capturedUsernames.contains(null));
        assertEquals(2, capturedUsernames.size());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userProfileInitializer.initialize(null));

        verifyNoInteractions(traineeDao);
        verifyNoInteractions(trainerDao);
        verifyNoInteractions(usernameGenerator);
        verifyNoInteractions(passwordGenerator);
    }

    private Trainee createTrainee(Long userId, String firstName, String lastName, String username) {
        return new Trainee(
                userId,
                firstName,
                lastName,
                username,
                "password123",
                true,
                LocalDate.of(2000, 1, 1),
                "Kyiv"
        );
    }

    private Trainer createTrainer(Long userId, String firstName, String lastName, String username) {
        return new Trainer(
                userId,
                firstName,
                lastName,
                username,
                "password123",
                true,
                new TrainingType(1L, "Fitness")
        );
    }
}