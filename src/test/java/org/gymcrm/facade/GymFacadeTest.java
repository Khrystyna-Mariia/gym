package org.gymcrm.facade;

import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {
    private GymFacade gymFacade;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(traineeService, trainerService, trainingService);
    }

    private Trainee createTrainee(Long userId, String firstName, String lastName) {
        return new Trainee(
                userId,
                firstName,
                lastName,
                firstName + "." + lastName,
                "password123",
                true,
                LocalDate.of(2000, 1, 1),
                "Kyiv"
        );
    }

    private Trainer createTrainer(Long userId, String firstName, String lastName) {
        return new Trainer(
                userId,
                firstName,
                lastName,
                firstName + "." + lastName,
                "password123",
                true,
                new TrainingType(1L, "Fitness")
        );
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

    @Test
    void shouldCreateTrainee() {
        Trainee trainee = createTrainee(1L, "John", "Smith");

        when(traineeService.create(trainee)).thenReturn(trainee);

        Trainee result = gymFacade.createTrainee(trainee);

        assertEquals(trainee, result);
        verify(traineeService).create(trainee);
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee(1L, "Anna", "Brown");

        when(traineeService.update(trainee)).thenReturn(trainee);

        Trainee result = gymFacade.updateTrainee(trainee);

        assertEquals(trainee, result);
        verify(traineeService).update(trainee);
    }

    @Test
    void shouldDeleteTrainee() {
        gymFacade.deleteTrainee(1L);

        verify(traineeService).delete(1L);
    }

    @Test
    void shouldGetTraineeById() {
        Trainee trainee = createTrainee(1L, "John", "Smith");

        when(traineeService.selectById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.getTrainee(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(traineeService).selectById(1L);
    }

    @Test
    void shouldGetAllTrainees() {
        Trainee firstTrainee = createTrainee(1L, "John", "Smith");
        Trainee secondTrainee = createTrainee(2L, "Anna", "Brown");

        when(traineeService.selectAll()).thenReturn(List.of(firstTrainee, secondTrainee));

        List<Trainee> result = gymFacade.getAllTrainees();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainee));
        assertTrue(result.contains(secondTrainee));
        verify(traineeService).selectAll();
    }

    @Test
    void shouldCreateTrainer() {
        Trainer trainer = createTrainer(1L, "Michael", "Green");

        when(trainerService.create(trainer)).thenReturn(trainer);

        Trainer result = gymFacade.createTrainer(trainer);

        assertEquals(trainer, result);
        verify(trainerService).create(trainer);
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer(1L, "Olivia", "White");

        when(trainerService.update(trainer)).thenReturn(trainer);

        Trainer result = gymFacade.updateTrainer(trainer);

        assertEquals(trainer, result);
        verify(trainerService).update(trainer);
    }

    @Test
    void shouldGetTrainerById() {
        Trainer trainer = createTrainer(1L, "Michael", "Green");

        when(trainerService.selectById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.getTrainer(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        verify(trainerService).selectById(1L);
    }

    @Test
    void shouldGetAllTrainers() {
        Trainer firstTrainer = createTrainer(1L, "Michael", "Green");
        Trainer secondTrainer = createTrainer(2L, "Olivia", "White");

        when(trainerService.selectAll()).thenReturn(List.of(firstTrainer, secondTrainer));

        List<Trainer> result = gymFacade.getAllTrainers();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTrainer));
        assertTrue(result.contains(secondTrainer));
        verify(trainerService).selectAll();
    }

    @Test
    void shouldCreateTraining() {
        Training training = createTraining(1L, "Morning Fitness");

        when(trainingService.create(training)).thenReturn(training);

        Training result = gymFacade.createTraining(training);

        assertEquals(training, result);
        verify(trainingService).create(training);
    }

    @Test
    void shouldGetTrainingById() {
        Training training = createTraining(1L, "Morning Fitness");

        when(trainingService.selectById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = gymFacade.getTraining(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
        verify(trainingService).selectById(1L);
    }

    @Test
    void shouldGetAllTrainings() {
        Training firstTraining = createTraining(1L, "Morning Fitness");
        Training secondTraining = createTraining(2L, "Evening Yoga");

        when(trainingService.selectAll()).thenReturn(List.of(firstTraining, secondTraining));

        List<Training> result = gymFacade.getAllTrainings();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstTraining));
        assertTrue(result.contains(secondTraining));
        verify(trainingService).selectAll();
    }

}