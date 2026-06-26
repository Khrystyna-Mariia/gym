package org.gymcrm.service.impl;

import org.gymcrm.dao.TraineeDao;
import org.gymcrm.model.Trainee;
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

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(traineeDao, userProfileInitializer);
    }

    @Test
    void shouldCreateTraineeUsingInitializerAndDao() {
        Trainee newTrainee = new Trainee(
                null,
                "John",
                "Smith",
                null,
                null,
                false,
                LocalDate.of(2001, 3, 10),
                "Ternopil"
        );

        Trainee savedTrainee = new Trainee(
                2L,
                "John",
                "Smith",
                "John.Smith1",
                "Generated1",
                true,
                LocalDate.of(2001, 3, 10),
                "Ternopil"
        );

        doAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.setUsername("John.Smith1");
            trainee.setPassword("Generated1");
            trainee.setActive(true);
            return null;
        }).when(userProfileInitializer).initialize(newTrainee);

        when(traineeDao.save(newTrainee)).thenReturn(savedTrainee);

        Trainee result = traineeService.create(newTrainee);

        assertEquals(2L, result.getUserId());
        assertEquals("John.Smith1", result.getUsername());
        assertEquals("Generated1", result.getPassword());
        assertTrue(result.isActive());

        verify(userProfileInitializer).initialize(newTrainee);
        verify(traineeDao).save(newTrainee);
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTrainee() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(null));

        verifyNoInteractions(userProfileInitializer);
        verifyNoInteractions(traineeDao);
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown", "Anna.Brown");

        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.update(trainee);

        assertEquals(trainee, result);
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldDeleteTrainee() {
        traineeService.delete(1L);

        verify(traineeDao).deleteById(1L);
    }

    @Test
    void shouldSelectTraineeById() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown", "Anna.Brown");

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
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
}