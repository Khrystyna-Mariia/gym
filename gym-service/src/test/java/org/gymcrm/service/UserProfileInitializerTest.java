package org.gymcrm.service;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Role;
import org.gymcrm.model.User;
import org.gymcrm.util.PasswordGenerator;
import org.gymcrm.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userProfileInitializer = new UserProfileInitializer(
                traineeDao,
                trainerDao,
                usernameGenerator,
                passwordGenerator,
                passwordEncoder
        );
    }

    @Test
    void shouldInitializeUserProfileWithUsernamePasswordAndActiveStatus() {
        User user = new User(null, "John", "Smith", null, null, false, null);

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any())).thenReturn("John.Smith1");
        when(passwordGenerator.generate()).thenReturn("GeneratedPassword123");
        when(passwordEncoder.encode("GeneratedPassword123")).thenReturn("EncodedPassword123");

        String rawPassword = userProfileInitializer.initialize(user, Role.TRAINEE);

        assertEquals("John.Smith1", user.getUsername());
        assertEquals("EncodedPassword123", user.getPassword());
        assertEquals("GeneratedPassword123", rawPassword);
        assertTrue(user.isActive());
        assertEquals(Role.TRAINEE, user.getRole());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();
        verify(passwordEncoder).encode("GeneratedPassword123");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldPassUsernameExistencePredicateToUsernameGenerator() {
        User user = new User(null, "Olivia", "White", null, null, false, null);

        when(usernameGenerator.generate(eq("Olivia"), eq("White"), any())).thenReturn("Olivia.White");
        when(passwordGenerator.generate()).thenReturn("GenPassword");
        when(passwordEncoder.encode("GenPassword")).thenReturn("EncodedGenPassword");

        userProfileInitializer.initialize(user, Role.TRAINER);

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
        assertThrows(ValidationException.class, () -> userProfileInitializer.initialize(null, Role.TRAINEE));

        verifyNoInteractions(traineeDao, trainerDao, usernameGenerator, passwordGenerator, passwordEncoder);
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        User user = new User(null, "John", "Smith", null, null, false, null);

        assertThrows(ValidationException.class, () -> userProfileInitializer.initialize(user, null));

        verifyNoInteractions(traineeDao, trainerDao, usernameGenerator, passwordGenerator, passwordEncoder);
    }
}