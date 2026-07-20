package org.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gymcrm.aspect.SecurityAspect;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.filter.AuthenticationContextFilter;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.gymcrm.service.TrainingTypeService;
import org.gymcrm.service.impl.TraineeServiceImpl;
import org.gymcrm.service.impl.TrainerServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TrainerControllerTest.TestConfig.class})
class TrainerControllerTest {

    @Configuration
    @EnableWebMvc
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {
        @Bean public TraineeService traineeService() { return mock(TraineeServiceImpl.class); }
        @Bean public TrainerService trainerService() { return mock(TrainerServiceImpl.class); }
        @Bean public TrainingService trainingService() { return mock(TrainingService.class); }
        @Bean public TrainingTypeService trainingTypeService() { return mock(TrainingTypeService.class); }
        @Bean public TrainerMapper trainerMapper() { return mock(TrainerMapper.class); }
        @Bean public TrainingMapper trainingMapper() { return mock(TrainingMapper.class); }

        @Bean
        public SecurityAspect securityAspect(TraineeService traineeService, TrainerService trainerService) {
            return new SecurityAspect(traineeService, trainerService);
        }

        @Bean
        public TrainerController trainerController(TrainerService trainerService, TrainingService trainingService,
                                                   TrainingTypeService trainingTypeService, TrainerMapper trainerMapper,
                                                   TrainingMapper trainingMapper) {
            return new TrainerController(trainerService, trainingService, trainingTypeService, trainerMapper, trainingMapper);
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @Autowired private WebApplicationContext context;

    @Autowired private TraineeService traineeServiceProxy;
    @Autowired private TrainerService trainerServiceProxy;
    @Autowired private TrainingService trainingServiceProxy;
    @Autowired private TrainingTypeService trainingTypeServiceProxy;
    @Autowired private TrainerMapper trainerMapperProxy;
    @Autowired private TrainingMapper trainingMapperProxy;

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private TrainingTypeService trainingTypeService;
    private TrainerMapper trainerMapper;
    private TrainingMapper trainingMapper;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String VALID_USERNAME = "anna.k";
    private final String VALID_PASSWORD = "password456";
    private final String AUTH_HEADER_VALUE = "Basic " + Base64.getEncoder()
            .encodeToString((VALID_USERNAME + ":" + VALID_PASSWORD).getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new AuthenticationContextFilter())
                .build();

        traineeService = AopTestUtils.getTargetObject(traineeServiceProxy);
        trainerService = AopTestUtils.getTargetObject(trainerServiceProxy);
        trainingService = AopTestUtils.getTargetObject(trainingServiceProxy);
        trainingTypeService = AopTestUtils.getTargetObject(trainingTypeServiceProxy);
        trainerMapper = AopTestUtils.getTargetObject(trainerMapperProxy);
        trainingMapper = AopTestUtils.getTargetObject(trainingMapperProxy);

        Mockito.reset(traineeService, trainerService, trainingService, trainingTypeService, trainerMapper, trainingMapper);
    }

    private void mockSuccessfulAuthentication() {
        when(traineeService.authenticate(VALID_USERNAME, VALID_PASSWORD)).thenReturn(false);
        when(trainerService.authenticate(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
    }

    @Test
    void unauthorizedAccess_blockedWithoutHeader() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/anna.k"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedAccess_blockedWithInvalidCredentials() throws Exception {
        when(traineeService.authenticate("bad.trainer", "wrong.pass")).thenReturn(false);
        when(trainerService.authenticate("bad.trainer", "wrong.pass")).thenReturn(false);

        String badAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString("bad.trainer:wrong.pass".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/trainers/anna.k")
                        .header("Authorization", badAuthHeader))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedAccess_blockedWithMalformedHeader() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/anna.k")
                        .header("Authorization", "Basic INVALID_BASE64_STRING"))
                .andExpect(status().isUnauthorized());
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
        mockSuccessfulAuthentication();
        Trainer trainer = new Trainer();
        TrainerProfileResponse response = new TrainerProfileResponse("Anna", "K", "YOGA", true, List.of());

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/anna.k")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("YOGA"));
    }

    @Test
    void getProfile_returnsNotFoundWhenTrainerMissing() throws Exception {
        mockSuccessfulAuthentication();
        when(trainerService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainers/ghost")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_updatesAndReturnsResponse() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("anna.k", "Anna", "K", true);
        Trainer existing = new Trainer();
        Trainer updated = new Trainer();
        UpdateTrainerProfileResponse response =
                new UpdateTrainerProfileResponse("anna.k", "Anna", "K", "YOGA", true, List.of());

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(existing));
        when(trainerService.update(existing)).thenReturn(updated);
        when(trainerMapper.toUpdateResponse(updated)).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        verify(trainerMapper).updateEntityFromRequest(request, existing);
    }

    @Test
    void updateProfile_returnsBadRequestWhenPathAndBodyUsernameMismatch() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("different.user", "Anna", "K", true);

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).update(any());
    }

    @Test
    void getTrainings_passesFiltersToService() throws Exception {
        mockSuccessfulAuthentication();
        when(trainingService.getTrainerTrainings(eq("anna.k"), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toTrainerTrainingResponseList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainers/anna.k/trainings")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .param("periodFrom", "2026-01-01")
                        .param("traineeName", "John"))
                .andExpect(status().isOk());

        verify(trainingService).getTrainerTrainings("anna.k", LocalDate.of(2026, 1, 1), null, "John");
    }

    @Test
    void updateStatus_activatesWhenIsActiveTrue() throws Exception {
        mockSuccessfulAuthentication();
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("anna.k", true);

        mockMvc.perform(patch("/api/v1/trainers/anna.k/status")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).activate("anna.k");
    }

    @Test
    void updateStatus_deactivatesWhenIsActiveFalse() throws Exception {
        mockSuccessfulAuthentication();
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("anna.k", false);

        mockMvc.perform(patch("/api/v1/trainers/anna.k/status")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).deactivate("anna.k");
    }

    @Test
    void updateProfile_returnsNotFoundWhenTrainerDoesNotExist() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("anna.k", "Anna", "K", true);

        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainers/anna.k")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}