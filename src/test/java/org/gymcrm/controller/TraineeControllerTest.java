package org.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gymcrm.aspect.SecurityAspect;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.filter.AuthenticationContextFilter;
import org.gymcrm.mapper.TraineeMapper;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TraineeControllerTest.TestConfig.class})
class TraineeControllerTest {

    @Configuration
    @EnableWebMvc
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {
        @Bean public TraineeService traineeService() { return mock(TraineeServiceImpl.class); }
        @Bean public TrainerService trainerService() { return mock(TrainerServiceImpl.class); }
        @Bean public TrainingService trainingService() { return mock(TrainingService.class); }
        @Bean public TraineeMapper traineeMapper() { return mock(TraineeMapper.class); }
        @Bean public TrainerMapper trainerMapper() { return mock(TrainerMapper.class); }
        @Bean public TrainingMapper trainingMapper() { return mock(TrainingMapper.class); }

        @Bean
        public SecurityAspect securityAspect(TraineeService traineeService, TrainerService trainerService) {
            return new SecurityAspect(traineeService, trainerService);
        }

        @Bean
        public TraineeController traineeController(TraineeService traineeService, TrainerService trainerService,
                                                   TrainingService trainingService, TraineeMapper traineeMapper,
                                                   TrainerMapper trainerMapper, TrainingMapper trainingMapper) {
            return new TraineeController(traineeService, trainerService, trainingService,
                    traineeMapper, trainerMapper, trainingMapper);
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
    @Autowired private TraineeMapper traineeMapperProxy;
    @Autowired private TrainerMapper trainerMapperProxy;
    @Autowired private TrainingMapper trainingMapperProxy;

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private TraineeMapper traineeMapper;
    private TrainerMapper trainerMapper;
    private TrainingMapper trainingMapper;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String VALID_USERNAME = "john.doe";
    private final String VALID_PASSWORD = "password123";
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
        traineeMapper = AopTestUtils.getTargetObject(traineeMapperProxy);
        trainerMapper = AopTestUtils.getTargetObject(trainerMapperProxy);
        trainingMapper = AopTestUtils.getTargetObject(trainingMapperProxy);

        Mockito.reset(traineeService, trainerService, trainingService, traineeMapper, trainerMapper, trainingMapper);
    }

    private void mockSuccessfulAuthentication() {
        when(traineeService.authenticate(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
        when(trainerService.authenticate(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
    }

    @Test
    void unauthorizedAccess_blockedWithoutHeader() throws Exception {
        mockMvc.perform(get("/api/v1/trainees/john.doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedAccess_blockedWithInvalidCredentials() throws Exception {
        when(traineeService.authenticate("bad.user", "wrong.password")).thenReturn(false);
        when(trainerService.authenticate("bad.user", "wrong.password")).thenReturn(false);

        String badAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString("bad.user:wrong.password".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/trainees/john.doe")
                        .header("Authorization", badAuthHeader))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedAccess_blockedWithMalformedHeader() throws Exception {
        mockMvc.perform(get("/api/v1/trainees/john.doe")
                        .header("Authorization", "Basic NOT_BASE64_STRING_!!!"))
                .andExpect(status().isUnauthorized());
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
        mockSuccessfulAuthentication();
        Trainee trainee = new Trainee();
        TraineeProfileResponse response = new TraineeProfileResponse(
                "John", "Doe", LocalDate.of(2000, 5, 14), "Main St 1", true, List.of());

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(traineeService, times(1)).authenticate(VALID_USERNAME, VALID_PASSWORD);
    }

    @Test
    void getProfile_returnsNotFoundWhenTraineeMissing() throws Exception {
        mockSuccessfulAuthentication();
        when(traineeService.selectByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/trainees/ghost")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_updatesAndReturnsResponse() throws Exception {
        mockSuccessfulAuthentication();
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
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("New addr"));

        verify(traineeMapper).updateEntityFromRequest(request, existing);
    }

    @Test
    void updateProfile_returnsBadRequestWhenPathAndBodyUsernameMismatch() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "different.user", "John", "Doe", null, null, true);

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).update(any());
    }

    @Test
    void deleteProfile_returnsOkAndDelegatesToService() throws Exception {
        mockSuccessfulAuthentication();

        mockMvc.perform(delete("/api/v1/trainees/john.doe")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk());

        verify(traineeService).deleteByUsername("john.doe");
    }

    @Test
    void getUnassignedTrainers_returnsListFromService() throws Exception {
        mockSuccessfulAuthentication();
        Trainer trainer = new Trainer();
        List<Trainer> trainers = List.of(trainer);
        List<TrainerShortInfo> response = List.of(new TrainerShortInfo("t1", "Anna", "K", "YOGA"));

        when(trainerService.getUnassignedTrainers("john.doe")).thenReturn(trainers);
        when(trainerMapper.toShortInfoList(trainers)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe/unassigned-trainers")
                        .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("t1"));
    }

    @Test
    void updateTrainersList_updatesAndReturnsNewList() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("john.doe", List.of("t1"));
        Trainee updated = new Trainee();
        updated.setTrainers(Set.of());
        List<TrainerShortInfo> response = List.of();

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(updated));
        when(trainerMapper.toShortInfoList(anyList())).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).updateTrainersList("john.doe", List.of("t1"));
    }

    @Test
    void getTrainings_passesFiltersToService() throws Exception {
        mockSuccessfulAuthentication();
        when(trainingService.getTraineeTrainings(eq("john.doe"), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toTraineeTrainingResponseList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainees/john.doe/trainings")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-12-31")
                        .param("trainerName", "Anna"))
                .andExpect(status().isOk());

        verify(trainingService).getTraineeTrainings("john.doe",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Anna", null);
    }

    @Test
    void updateStatus_activatesWhenIsActiveTrue() throws Exception {
        mockSuccessfulAuthentication();
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("john.doe", true);

        mockMvc.perform(patch("/api/v1/trainees/john.doe/status")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).activate("john.doe");
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    void updateStatus_deactivatesWhenIsActiveFalse() throws Exception {
        mockSuccessfulAuthentication();
        ActivateDeactivateRequest request = new ActivateDeactivateRequest("john.doe", false);

        mockMvc.perform(patch("/api/v1/trainees/john.doe/status")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).deactivate("john.doe");
    }

    @Test
    void updateProfile_returnsNotFoundWhenTraineeDoesNotExist() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "john.doe", "John", "Doe", LocalDate.of(2000, 5, 14), "New addr", true);

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainersList_returnsNotFoundWhenTraineeDoesNotExist() throws Exception {
        mockSuccessfulAuthentication();
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest("john.doe", List.of("t1"));

        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                        .header("Authorization", AUTH_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}