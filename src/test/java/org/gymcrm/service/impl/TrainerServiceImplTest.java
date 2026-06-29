package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainerDao;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.UserProfileInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(trainerDao, userProfileInitializer);
    }

    @Test
    void shouldCreateTrainerUsingInitializerAndDao() {
        Trainer newTrainer = new Trainer(
                null,
                "John",
                "Smith",
                null,
                null,
                false,
                new TrainingType(3L, "Strength")
        );

        Trainer savedTrainer = new Trainer(
                2L,
                "John",
                "Smith",
                "John.Smith1",
                "Generated1",
                true,
                new TrainingType(3L, "Strength")
        );

        doAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            trainer.setUsername("John.Smith1");
            trainer.setPassword("Generated1");
            trainer.setActive(true);
            return null;
        }).when(userProfileInitializer).initialize(newTrainer);

        when(trainerDao.save(newTrainer)).thenReturn(savedTrainer);

        Trainer result = trainerService.create(newTrainer);

        assertEquals(2L, result.getUserId());
        assertEquals("John.Smith1", result.getUsername());
        assertEquals("Generated1", result.getPassword());
        assertTrue(result.isActive());

        verify(userProfileInitializer).initialize(newTrainer);
        verify(trainerDao).save(newTrainer);
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTrainer() {
        assertThrows(ValidationException.class, () -> trainerService.create(null));

        verifyNoInteractions(userProfileInitializer);
        verifyNoInteractions(trainerDao);
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer(1L, "Michael", "Green", "Michael.Green");

        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer result = trainerService.update(trainer);

        assertEquals(trainer, result);
        verify(trainerDao).update(trainer);
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