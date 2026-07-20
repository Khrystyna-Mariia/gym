package org.gymcrm.controller;

import org.gymcrm.exception.GlobalExceptionHandler;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_returnsOkWhenTraineeCredentialsValid() throws Exception {
        when(traineeService.authenticate("john.doe", "pass123")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "john.doe")
                        .param("password", "pass123"))
                .andExpect(status().isOk());

        verify(trainerService, never()).authenticate(any(), any());
    }

    @Test
    void login_fallsBackToTrainerWhenNotTrainee() throws Exception {
        when(traineeService.authenticate("mike.smith", "pass123")).thenReturn(false);
        when(trainerService.authenticate("mike.smith", "pass123")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "mike.smith")
                        .param("password", "pass123"))
                .andExpect(status().isOk());
    }

    @Test
    void login_returnsUnauthorizedWhenCredentialsInvalid() throws Exception {
        when(traineeService.authenticate("john.doe", "wrong")).thenReturn(false);
        when(trainerService.authenticate("john.doe", "wrong")).thenReturn(false);

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "john.doe")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeLogin_updatesTraineePasswordWhenUsernameBelongsToTrainee() throws Exception {
        when(traineeService.selectByUsername("john.doe")).thenReturn(Optional.of(new Trainee()));

        String body = """
                {"username":"john.doe","oldPassword":"old123","newPassword":"new456"}
                """;

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("john.doe", "old123", "new456");
        verify(trainerService, never()).selectByUsername(any());
    }

    @Test
    void changeLogin_updatesTrainerPasswordWhenUsernameBelongsToTrainer() throws Exception {
        when(traineeService.selectByUsername("anna.k")).thenReturn(Optional.empty());
        when(trainerService.selectByUsername("anna.k")).thenReturn(Optional.of(new Trainer()));

        String body = """
            {"username":"anna.k","oldPassword":"old123","newPassword":"new456"}
            """;

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("anna.k", "old123", "new456");
    }

    @Test
    void changeLogin_returnsNotFoundWhenUsernameDoesNotExist() throws Exception {
        when(traineeService.selectByUsername("ghost")).thenReturn(Optional.empty());
        when(trainerService.selectByUsername("ghost")).thenReturn(Optional.empty());

        String body = """
                {"username":"ghost","oldPassword":"old123","newPassword":"new456"}
                """;

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}