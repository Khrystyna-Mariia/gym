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
import java.util.function.Predicate;

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
        Trainee newTrainee = createTrainee(null, "John", "Smith", null);
        newTrainee.setPassword(null);
        newTrainee.setActive(false);

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainee);

        assertEquals("John.Smith1", newTrainee.getUsername());
        assertEquals("Generated1", newTrainee.getPassword());
        assertTrue(newTrainee.isActive());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();

        verify(traineeDao, never()).findAll();
        verify(trainerDao, never()).findAll();
    }

    @Test
    void shouldInitializeTrainerProfileWithUsernamePasswordAndActiveStatus() {
        Trainer newTrainer = createTrainer(null, "John", "Smith", null);
        newTrainer.setPassword(null);
        newTrainer.setActive(false);

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainer);

        assertEquals("John.Smith1", newTrainer.getUsername());
        assertEquals("Generated1", newTrainer.getPassword());
        assertTrue(newTrainer.isActive());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();

        verify(traineeDao, never()).findAll();
        verify(trainerDao, never()).findAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldPassUsernameExistencePredicateToUsernameGenerator() {
        Trainee newTrainee = createTrainee(null, "Olivia", "White", null);

        when(usernameGenerator.generate(eq("Olivia"), eq("White"), any())).thenReturn("Olivia.White");
        when(passwordGenerator.generate()).thenReturn("Generated1");

        userProfileInitializer.initialize(newTrainee);

        ArgumentCaptor<Predicate<String>> predicateCaptor =
                ArgumentCaptor.forClass((Class) Predicate.class);

        verify(usernameGenerator).generate(eq("Olivia"), eq("White"), predicateCaptor.capture());

        Predicate<String> usernameExists = predicateCaptor.getValue();

        when(traineeDao.existsByUsername("John.Smith")).thenReturn(true);
        assertTrue(usernameExists.test("John.Smith"));
        verify(traineeDao).existsByUsername("John.Smith");
        verify(trainerDao, never()).existsByUsername("John.Smith");

        when(traineeDao.existsByUsername("Michael.Green")).thenReturn(false);
        when(trainerDao.existsByUsername("Michael.Green")).thenReturn(true);
        assertTrue(usernameExists.test("Michael.Green"));
        verify(traineeDao).existsByUsername("Michael.Green");
        verify(trainerDao).existsByUsername("Michael.Green");

        when(traineeDao.existsByUsername("Anna.Brown")).thenReturn(false);
        when(trainerDao.existsByUsername("Anna.Brown")).thenReturn(false);
        assertFalse(usernameExists.test("Anna.Brown"));
        verify(traineeDao).existsByUsername("Anna.Brown");
        verify(trainerDao).existsByUsername("Anna.Brown");
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