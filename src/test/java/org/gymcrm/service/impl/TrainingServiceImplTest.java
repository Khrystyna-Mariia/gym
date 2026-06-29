package org.gymcrm.service.impl;

import org.gymcrm.dao.TrainingDao;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
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
class TrainingServiceImplTest {
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl(trainingDao, traineeService, trainerService);
    }

    @Test
    void shouldCreateTrainingUsingDao() {
        Training newTraining = createTraining(null, "Evening Yoga");
        Training savedTraining = createTraining(2L, "Evening Yoga");

        when(traineeService.selectById(1L)).thenReturn(Optional.of(createTrainee()));
        when(trainerService.selectById(1L)).thenReturn(Optional.of(createTrainer()));
        when(trainingDao.save(newTraining)).thenReturn(savedTraining);

        Training result = trainingService.create(newTraining);

        assertEquals(2L, result.getId());
        assertEquals("Evening Yoga", result.getTrainingName());

        verify(traineeService).selectById(1L);
        verify(trainerService).selectById(1L);
        verify(trainingDao).save(newTraining);
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullTraining() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.create(null));

        verifyNoInteractions(trainingDao);
        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
    }

    @Test
    void shouldThrowExceptionWhenTraineeDoesNotExist() {
        Training training = createTraining(null, "Evening Yoga");

        when(traineeService.selectById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));

        verify(traineeService).selectById(1L);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingDao);
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExist() {
        Training training = createTraining(null, "Evening Yoga");

        when(traineeService.selectById(1L)).thenReturn(Optional.of(createTrainee()));
        when(trainerService.selectById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));

        verify(traineeService).selectById(1L);
        verify(trainerService).selectById(1L);
        verifyNoInteractions(trainingDao);
    }

    @Test
    void shouldSelectTrainingById() {
        Training training = createTraining(1L, "Morning Fitness");

        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.selectById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());

        verify(trainingDao).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenTrainingNotFound() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.selectById(99L);

        assertTrue(result.isEmpty());

        verify(trainingDao).findById(99L);
    }

    @Test
    void shouldSelectAllTrainings() {
        Training firstTraining = createTraining(1L, "Morning Fitness");
        Training secondTraining = createTraining(2L, "Evening Yoga");

        when(trainingDao.findAll()).thenReturn(List.of(firstTraining, secondTraining));

        List<Training> result = trainingService.selectAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTraining));
        assertTrue(result.contains(secondTraining));

        verify(trainingDao).findAll();
    }

    private Training createTraining(Long id, String trainingName) {
        return new Training(
                id,
                1L,
                1L,
                trainingName,
                1L,
                LocalDate.of(2026, 6, 24),
                60
        );
    }

    private Trainee createTrainee() {
        return new Trainee(
                1L,
                "John",
                "Smith",
                "John.Smith",
                "password123",
                true,
                LocalDate.of(2000, 1, 1),
                "Kyiv"
        );
    }

    private Trainer createTrainer() {
        return new Trainer(
                1L,
                "Michael",
                "Green",
                "Michael.Green",
                "password123",
                true,
                new TrainingType(1L, "Fitness")
        );
    }
}