package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.dto.request.*;
import org.gymcrm.dto.response.*;
import org.gymcrm.exception.ValidationException;
import org.gymcrm.mapper.TraineeMapper;
import org.gymcrm.mapper.TrainerMapper;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainee;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@Tag(name = "Trainees", description = "Trainee registration, profile management and trainings")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public TraineeController(TraineeService traineeService, TrainerService trainerService,
                             TrainingService trainingService, TraineeMapper traineeMapper,
                             TrainerMapper trainerMapper, TrainingMapper trainingMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new trainee", description = "Public endpoint, no authentication required")
    public RegistrationResponse register(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee trainee = traineeMapper.toEntity(request);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        Trainee created = traineeService.create(trainee);
        return traineeMapper.toRegistrationResponse(created);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public TraineeProfileResponse getProfile(@PathVariable String username) {
        Trainee trainee = traineeService.selectByUsername(username)
                .orElseThrow(() -> new org.gymcrm.exception.EntityNotFoundException(
                        "Trainee with username " + username + " not found"));
        return traineeMapper.toProfileResponse(trainee);
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public UpdateTraineeProfileResponse updateProfile(@PathVariable String username,
                                                      @Valid @RequestBody UpdateTraineeProfileRequest request) {
        requireUsernameMatch(username, request.username());
        Trainee trainee = traineeService.selectByUsername(username)
                .orElseThrow(() -> new org.gymcrm.exception.EntityNotFoundException(
                        "Trainee with username " + username + " not found"));
        traineeMapper.updateEntityFromRequest(request, trainee);
        Trainee updated = traineeService.update(trainee);
        return traineeMapper.toUpdateResponse(updated);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete trainee profile", description = "Hard delete, cascades to trainings")
    public void deleteProfile(@PathVariable String username) {
        traineeService.deleteByUsername(username);
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get active trainers not yet assigned to this trainee")
    public List<TrainerShortInfo> getUnassignedTrainers(@PathVariable String username) {
        return trainerMapper.toShortInfoList(trainerService.getUnassignedTrainers(username));
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainers list")
    public List<TrainerShortInfo> updateTrainersList(@PathVariable String username,
                                                     @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        requireUsernameMatch(username, request.traineeUsername());
        traineeService.updateTrainersList(username, request.trainerUsernames());
        Trainee updated = traineeService.selectByUsername(username)
                .orElseThrow(() -> new org.gymcrm.exception.EntityNotFoundException(
                        "Trainee with username " + username + " not found"));
        return trainerMapper.toShortInfoList(List.copyOf(updated.getTrainers()));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's trainings list with optional filters")
    public List<TraineeTrainingResponse> getTrainings(
            @PathVariable String username,
            @Parameter(description = "Period start date, inclusive")
            @RequestParam(required = false) LocalDate periodFrom,
            @Parameter(description = "Period end date, inclusive")
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        return trainingMapper.toTraineeTrainingResponseList(
                trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainee account", description = "Not idempotent")
    public void updateStatus(@PathVariable String username, @Valid @RequestBody ActivateDeactivateRequest request) {
        requireUsernameMatch(username, request.username());
        if (Boolean.TRUE.equals(request.isActive())) {
            traineeService.activate(username);
        } else {
            traineeService.deactivate(username);
        }
    }

    private void requireUsernameMatch(String pathUsername, String bodyUsername) {
        if (!pathUsername.equals(bodyUsername)) {
            throw new ValidationException("Username in path must match username in request body");
        }
    }
}