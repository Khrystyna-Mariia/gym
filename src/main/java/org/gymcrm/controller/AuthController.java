package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.dto.request.ChangeLoginRequest;
import org.gymcrm.dto.request.LoginRequest;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login and password management, shared between trainees and trainers")
public class AuthController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public AuthController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @PostMapping("/login")
    @Operation(summary = "Verify username/password match", description = "Public endpoint; used to check credentials before subsequent authenticated calls")
    public void login(@Valid @RequestBody LoginRequest request) {
        boolean valid = traineeService.authenticate(request.username(), request.password())
                || trainerService.authenticate(request.username(), request.password());
        if (!valid) {
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @PutMapping("/password")
    @Operation(summary = "Change password for a trainee or trainer account")
    public void changePassword(@Valid @RequestBody ChangeLoginRequest request) {
        String username = request.username();

        if (traineeService.selectByUsername(username).isPresent()) {
            traineeService.changePassword(username, request.oldPassword(), request.newPassword());
            return;
        }
        if (trainerService.selectByUsername(username).isPresent()) {
            trainerService.changePassword(username, request.oldPassword(), request.newPassword());
            return;
        }
        throw new EntityNotFoundException("User with username " + username + " not found");
    }
}