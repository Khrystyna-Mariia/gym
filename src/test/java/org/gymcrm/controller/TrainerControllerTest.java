package org.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.gymcrm.service.TrainingTypeService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TrainingTypeService trainingTypeService;
    @Mock private TrainerMapper trainerMapper;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainerController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_createsTrainerAndReturnsCredentials() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Anna", "K", 1L);
        TrainingType specialization = new TrainingType(1L, TrainingTypeEnum.YOGA);
        Trainer entity = new Trainer();
        Trainer saved = new Trainer();
        RegistrationResponse response = new RegistrationResponse("anna.k", "genPass456");

        when(trainingTypeService.selectById(1L)).thenReturn(Optional.of(specialization));
        when(trainerMapper.toEntity(request)).thenReturn(entity);
        when(trainerService.create(entity)).thenReturn(saved);
        when(trainerMapper.toRegistrationResponse(saved)).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("anna.k"));
    }

    @Test
    void register_returnsBadRequestWhenSpecializationDoesNotExist() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Anna", "K", 999L);
        when(trainingTypeService.selectById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getProfile_returnsProfileWhenTrainerExists() throws Exception {
        Trainer trainer = new Trainer();
        TrainerProfileResponse response = new TrainerProfileResponse("Anna", "K", "YOGA", true, List.of());

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/anna.k"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("YOGA"));
    }

    @Test
    void getProfile_returnsNotFoundWhenTrainerMissing() throws Exception {
        when(trainerService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainers/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_updatesAndReturnsResponse() throws Exception {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("anna.k", "Anna", "K", true);
        Trainer existing = new Trainer();
        Trainer updated = new Trainer();
        UpdateTrainerProfileResponse response =
                new UpdateTrainerProfileResponse("anna.k", "Anna", "K", "YOGA", true, List.of());

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(existing));
        when(trainerService.update(existing)).thenReturn(updated);
        when(trainerMapper.toUpdateResponse(updated)).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        verify(trainerMapper).updateEntityFromRequest(request, existing);
    }

    @Test
    void updateProfile_returnsBadRequestWhenPathAndBodyUsernameMismatch() throws Exception {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("different.user", "Anna", "K", true);

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainings_passesFiltersToService() throws Exception {
        when(trainingService.getTrainerTrainings(eq("anna.k"), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toTrainerTrainingResponseList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainers/anna.k/trainings")
                        .param("periodFrom", "2026-01-01")
                        .param("traineeName", "John"))
                .andExpect(status().isOk());

        verify(trainingService).getTrainerTrainings("anna.k", LocalDate.of(2026, 1, 1), null, "John");
    }

    @Test
    void updateStatus_activatesWhenIsActiveTrue() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("anna.k", true);

        mockMvc.perform(patch("/api/v1/trainers/anna.k/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).activate("anna.k");
    }

    @Test
    void updateStatus_deactivatesWhenIsActiveFalse() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("anna.k", false);

        mockMvc.perform(patch("/api/v1/trainers/anna.k/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).deactivate("anna.k");
    }

    @Test
    void updateProfile_returnsNotFoundWhenTrainerDoesNotExist() throws Exception {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("anna.k", "Anna", "K", true);

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}