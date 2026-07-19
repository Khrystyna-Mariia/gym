package org.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.mapper.TraineeMapper;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TraineeMapper traineeMapper;
    @Mock private TrainerMapper trainerMapper;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TraineeController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_createsTraineeAndReturnsCredentials() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(2000, 5, 14), "Main St 1");
        Trainee entity = new Trainee();
        Trainee saved = new Trainee();
        RegistrationResponse response = new RegistrationResponse("john.doe", "genPass123");

        when(traineeMapper.toEntity(request)).thenReturn(entity);
        when(traineeService.create(entity)).thenReturn(saved);
        when(traineeMapper.toRegistrationResponse(saved)).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("genPass123"));
    }

    @Test
    void register_returnsBadRequestWhenFirstNameMissing() throws Exception {
        String body = """
                {"lastName":"Doe"}
                """;

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void getProfile_returnsProfileWhenTraineeExists() throws Exception {
        Trainee trainee = new Trainee();
        TraineeProfileResponse response = new TraineeProfileResponse(
                "John", "Doe", LocalDate.of(2000, 5, 14), "Main St 1", true, List.of());

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getProfile_returnsNotFoundWhenTraineeMissing() throws Exception {
        when(traineeService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainees/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_updatesAndReturnsResponse() throws Exception {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "john.doe", "John", "Doe", LocalDate.of(2000, 5, 14), "New addr", true);
        Trainee existing = new Trainee();
        Trainee updated = new Trainee();
        UpdateTraineeProfileResponse response = new UpdateTraineeProfileResponse(
                "john.doe", "John", "Doe", LocalDate.of(2000, 5, 14), "New addr", true, List.of());

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(traineeService.update(existing)).thenReturn(updated);
        when(traineeMapper.toUpdateResponse(updated)).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("New addr"));

        verify(traineeMapper).updateEntityFromRequest(request, existing);
    }

    @Test
    void updateProfile_returnsBadRequestWhenPathAndBodyUsernameMismatch() throws Exception {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "different.user", "John", "Doe", null, null, true);

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void deleteProfile_returnsOkAndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees/john.doe"))
                .andExpect(status().isOk());

        verify(traineeService).deleteByUsername("john.doe");
    }

    @Test
    void getUnassignedTrainers_returnsListFromService() throws Exception {
        Trainer trainer = new Trainer();
        List<Trainer> trainers = List.of(trainer);
        List<TrainerShortInfo> response = List.of(new TrainerShortInfo("t1", "Anna", "K", "YOGA"));

        when(trainerService.getUnassignedTrainers("john.doe")).thenReturn(trainers);
        when(trainerMapper.toShortInfoList(trainers)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe/unassigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("t1"));
    }

    @Test
    void updateTrainersList_updatesAndReturnsNewList() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("john.doe", List.of("t1"));
        Trainee updated = new Trainee();
        updated.setTrainers(Set.of());
        List<TrainerShortInfo> response = List.of();

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(updated));
        when(trainerMapper.toShortInfoList(anyList())).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).updateTrainersList("john.doe", List.of("t1"));
    }

    @Test
    void getTrainings_passesFiltersToService() throws Exception {
        when(trainingService.getTraineeTrainings(eq("john.doe"), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toTraineeTrainingResponseList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainees/john.doe/trainings")
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-12-31")
                        .param("trainerName", "Anna"))
                .andExpect(status().isOk());

        verify(trainingService).getTraineeTrainings("john.doe",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Anna", null);
    }

    @Test
    void updateStatus_activatesWhenIsActiveTrue() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("john.doe", true);

        mockMvc.perform(patch("/api/v1/trainees/john.doe/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).activate("john.doe");
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    void updateStatus_deactivatesWhenIsActiveFalse() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("john.doe", false);

        mockMvc.perform(patch("/api/v1/trainees/john.doe/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).deactivate("john.doe");
    }

    @Test
    void updateProfile_returnsNotFoundWhenTraineeDoesNotExist() throws Exception {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "john.doe", "John", "Doe", LocalDate.of(2000, 5, 14), "New addr", true);

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainersList_returnsNotFoundWhenTraineeDoesNotExist() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("john.doe", List.of("t1"));

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}