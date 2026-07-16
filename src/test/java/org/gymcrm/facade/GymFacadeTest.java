package org.gymcrm.facade;

import org.gymcrm.model.*;
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

    private Trainee createTrainee(Long id, String firstName, String lastName) {
        User user = new User(id, firstName, lastName, firstName + "." + lastName, "password123", true);
        Trainee trainee = new Trainee();
        trainee.setId(id);
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Kyiv");
        return trainee;
    }

    private Trainer createTrainer(Long id, String firstName, String lastName) {
        User user = new User(id, firstName, lastName, firstName + "." + lastName, "password123", true);
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.FITNESS));
        return trainer;
    }

    private Training createTraining(Long id, String trainingName) {
        Training training = new Training();
        training.setId(id);
        training.setTrainingName(trainingName);
        training.setTrainee(createTrainee(1L, "John", "Smith"));
        training.setTrainer(createTrainer(1L, "Michael", "Green"));
        training.setTrainingDate(LocalDate.of(2026, 6, 24));
        training.setTrainingDuration(60);
        training.setTrainingType(new TrainingType(1L, TrainingTypeEnum.FITNESS));
        return training;
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
        String username = "John.Smith";

        gymFacade.deleteTrainee(username);

        verify(traineeService).deleteByUsername(username);
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
    void shouldGetTraineeByUsername() {
        String username = "John.Smith";
        Trainee trainee = createTrainee(1L, "John", "Smith");
        when(traineeService.selectByUsername(username)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.getTraineeByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(traineeService).selectByUsername(username);
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
    void shouldAuthenticateTrainee() {
        String username = "John.Smith";
        String password = "password123";
        when(traineeService.authenticate(username, password)).thenReturn(true);

        boolean result = gymFacade.authenticateTrainee(username, password);

        assertTrue(result);
        verify(traineeService).authenticate(username, password);
    }

    @Test
    void shouldChangeTraineePassword() {
        String username = "John.Smith";

        gymFacade.changeTraineePassword(username, "oldPass", "newPass");

        verify(traineeService).changePassword(username, "oldPass", "newPass");
    }

    @Test
    void shouldActivateTrainee() {
        String username = "John.Smith";

        gymFacade.activateTrainee(username);

        verify(traineeService).activate(username);
    }

    @Test
    void shouldDeactivateTrainee() {
        String username = "John.Smith";

        gymFacade.deactivateTrainee(username);

        verify(traineeService).deactivate(username);
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        String username = "John.Smith";
        List<String> trainers = List.of("Trainer.One", "Trainer.Two");

        gymFacade.updateTraineeTrainersList(username, trainers);

        verify(traineeService).updateTrainersList(username, trainers);
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
    void shouldGetTrainerByUsername() {
        String username = "Michael.Green";
        Trainer trainer = createTrainer(1L, "Michael", "Green");
        when(trainerService.selectByUsername(username)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.getTrainerByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        verify(trainerService).selectByUsername(username);
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
    void shouldGetUnassignedTrainers() {
        String username = "John.Smith";
        List<Trainer> trainers = List.of(createTrainer(2L, "Olivia", "White"));
        when(trainerService.getUnassignedTrainers(username)).thenReturn(trainers);

        List<Trainer> result = gymFacade.getUnassignedTrainers(username);

        assertEquals(trainers, result);
        verify(trainerService).getUnassignedTrainers(username);
    }

    @Test
    void shouldAuthenticateTrainer() {
        String username = "Michael.Green";
        String password = "password123";
        when(trainerService.authenticate(username, password)).thenReturn(true);

        boolean result = gymFacade.authenticateTrainer(username, password);

        assertTrue(result);
        verify(trainerService).authenticate(username, password);
    }

    @Test
    void shouldChangeTrainerPassword() {
        String username = "Michael.Green";

        gymFacade.changeTrainerPassword(username, "oldPass", "newPass");

        verify(trainerService).changePassword(username, "oldPass", "newPass");
    }

    @Test
    void shouldActivateTrainer() {
        String username = "Michael.Green";

        gymFacade.activateTrainer(username);

        verify(trainerService).activate(username);
    }

    @Test
    void shouldDeactivateTrainer() {
        String username = "Michael.Green";

        gymFacade.deactivateTrainer(username);

        verify(trainerService).deactivate(username);
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

    @Test
    void shouldGetTraineeTrainings() {
        String username = "John.Smith";
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        List<Training> trainings = List.of(createTraining(1L, "Morning Fitness"));

        when(trainingService.getTraineeTrainings(username, from, to, "Michael", "Fitness"))
                .thenReturn(trainings);

        List<Training> result = gymFacade.getTraineeTrainings(username, from, to, "Michael", "Fitness");

        assertEquals(trainings, result);
        verify(trainingService).getTraineeTrainings(username, from, to, "Michael", "Fitness");
    }

    @Test
    void shouldGetTrainerTrainings() {
        String username = "Michael.Green";
        List<Training> trainings = List.of(createTraining(1L, "Morning Fitness"));

        when(trainingService.getTrainerTrainings(username, null, null, "John"))
                .thenReturn(trainings);

        List<Training> result = gymFacade.getTrainerTrainings(username, null, null, "John");

        assertEquals(trainings, result);
        verify(trainingService).getTrainerTrainings(username, null, null, "John");
    }
}