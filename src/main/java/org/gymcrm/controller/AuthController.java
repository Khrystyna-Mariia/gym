package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.actuator.GymMetrics;
import org.gymcrm.dto.request.ChangeLoginRequest;
import org.gymcrm.dto.request.LoginRequest;
import org.gymcrm.dto.response.LoginResponse;
import org.gymcrm.exception.AuthenticationException;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.model.Role;
import org.gymcrm.security.JwtService;
import org.gymcrm.security.UserPrincipal;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login and password management, shared between trainees and trainers")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final GymMetrics gymMetrics;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          TraineeService traineeService, TrainerService trainerService, GymMetrics gymMetrics) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.gymMetrics = gymMetrics;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT bearer token", description = "Public endpoint")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            gymMetrics.incrementLoginSuccess();
            return new LoginResponse(token);
        } catch (BadCredentialsException e) {
            gymMetrics.incrementLoginFailure();
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @PutMapping("/password")
    @Operation(summary = "Change password for the currently authenticated account")
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody ChangeLoginRequest request) {
        String username = principal.getUsername();

        if (principal.user().getRole() == Role.TRAINEE) {
            traineeService.changePassword(username, request.oldPassword(), request.newPassword());
        } else {
            trainerService.changePassword(username, request.oldPassword(), request.newPassword());
        }
    }
}