package org.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gymcrm.dto.request.AddTrainingRequest;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock private TrainingService trainingService;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addTraining_createsTrainingWithTrainerSpecializationAsType() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "john.doe", "anna.k", "Morning Yoga", LocalDate.of(2026, 8, 1), 60);

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType yoga = new TrainingType(1L, TrainingTypeEnum.YOGA);
        trainer.setSpecialization(yoga);
        Training mappedEntity = new Training();

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(request)).thenReturn(mappedEntity);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingService).create(captor.capture());
        Training created = captor.getValue();

        assertEquals(trainee, created.getTrainee());
        assertEquals(trainer, created.getTrainer());
        assertEquals(yoga, created.getTrainingType());
    }

    @Test
    void addTraining_returnsNotFoundWhenTraineeDoesNotExist() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "ghost", "anna.k", "Morning Yoga", LocalDate.of(2026, 8, 1), 60);

        when(traineeService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(trainingService);
        verify(trainerService, never()).selectByUsername(any());
    }

    @Test
    void addTraining_returnsNotFoundWhenTrainerDoesNotExist() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "john.doe", "ghost", "Morning Yoga", LocalDate.of(2026, 8, 1), 60);

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(new Trainee()));
        when(trainerService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(trainingService);
    }

    @Test
    void addTraining_returnsBadRequestWhenDurationIsZero() throws Exception {
        String body = """
                {"traineeUsername":"john.doe","trainerUsername":"anna.k",
                 "trainingName":"Morning Yoga","trainingDate":"2026-08-01","trainingDuration":0}
                """;

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService, trainerService, trainingService);
    }
}