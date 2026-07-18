package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.gymcrm.service.TrainingTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainers", description = "Trainer registration, profile management and trainings")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public TrainerController(TrainerService trainerService, TrainingService trainingService,
                             TrainingTypeService trainingTypeService, TrainerMapper trainerMapper,
                             TrainingMapper trainingMapper) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new trainer", description = "Public endpoint, no authentication required")
    public RegistrationResponse register(@Valid @RequestBody TrainerRegistrationRequest request) {
        TrainingType specialization = trainingTypeService.selectById(request.specializationId())
                .orElseThrow(() -> new ValidationException(
                        "Training type with id " + request.specializationId() + " does not exist"));
        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(specialization);
        Trainer created = trainerService.create(trainer);
        return trainerMapper.toRegistrationResponse(created);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    public TrainerProfileResponse getProfile(@PathVariable String username) {
        Trainer trainer = trainerService.selectByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username " + username + " not found"));
        return trainerMapper.toProfileResponse(trainer);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile", description = "Specialization is read-only and cannot be changed")
    public UpdateTrainerProfileResponse updateProfile(@PathVariable String username,
                                                      @Valid @RequestBody UpdateTrainerProfileRequest request) {
        requireUsernameMatch(username, request.username());
        Trainer trainer = trainerService.selectByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username " + username + " not found"));
        trainerMapper.updateEntityFromRequest(request, trainer);
        Trainer updated = trainerService.update(trainer);
        return trainerMapper.toUpdateResponse(updated);
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer's trainings list with optional filters")
    public List<TrainerTrainingResponse> getTrainings(
            @PathVariable String username,
            @Parameter(description = "Period start date, inclusive")
            @RequestParam(required = false) LocalDate periodFrom,
            @Parameter(description = "Period end date, inclusive")
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        return trainingMapper.toTrainerTrainingResponseList(
                trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainer account", description = "Not idempotent")
    public void updateStatus(@PathVariable String username, @Valid @RequestBody ActivateDeactivateRequest request) {
        requireUsernameMatch(username, request.username());
        if (Boolean.TRUE.equals(request.isActive())) {
            trainerService.activate(username);
        } else {
            trainerService.deactivate(username);
        }
    }

    private void requireUsernameMatch(String pathUsername, String bodyUsername) {
        if (!pathUsername.equals(bodyUsername)) {
            throw new ValidationException("Username in path must match username in request body");
        }
    }
}