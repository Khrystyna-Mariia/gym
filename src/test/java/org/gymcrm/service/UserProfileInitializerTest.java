package org.gymcrm.service;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.User;
import org.gymcrm.util.PasswordGenerator;
import org.gymcrm.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldInitializeUserProfileWithUsernamePasswordAndActiveStatus() {
        User user = new User(null, "John", "Smith", null, null, false);

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("GeneratedPassword123");

        userProfileInitializer.initialize(user);

        assertEquals("John.Smith1", user.getUsername());
        assertEquals("GeneratedPassword123", user.getPassword());
        assertTrue(user.isActive());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldPassUsernameExistencePredicateToUsernameGenerator() {
        User user = new User(null, "Olivia", "White", null, null, false);

        when(usernameGenerator.generate(eq("Olivia"), eq("White"), any())).thenReturn("Olivia.White");
        when(passwordGenerator.generate()).thenReturn("GenPassword");

        userProfileInitializer.initialize(user);

        ArgumentCaptor<Predicate<String>> predicateCaptor = ArgumentCaptor.forClass((Class) Predicate.class);
        verify(usernameGenerator).generate(eq("Olivia"), eq("White"), predicateCaptor.capture());

        Predicate<String> usernameExistsPredicate = predicateCaptor.getValue();

        when(traineeDao.existsByUsername("existing.trainee")).thenReturn(true);
        assertTrue(usernameExistsPredicate.test("existing.trainee"));
        verify(traineeDao).existsByUsername("existing.trainee");
        verify(trainerDao, never()).existsByUsername("existing.trainee");

        when(traineeDao.existsByUsername("existing.trainer")).thenReturn(false);
        when(trainerDao.existsByUsername("existing.trainer")).thenReturn(true);
        assertTrue(usernameExistsPredicate.test("existing.trainer"));

        when(traineeDao.existsByUsername("free.username")).thenReturn(false);
        when(trainerDao.existsByUsername("free.username")).thenReturn(false);
        assertFalse(usernameExistsPredicate.test("free.username"));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        assertThrows(ValidationException.class, () -> userProfileInitializer.initialize(null));

        verifyNoInteractions(traineeDao);
        verifyNoInteractions(trainerDao);
        verifyNoInteractions(usernameGenerator);
        verifyNoInteractions(passwordGenerator);
    }

}