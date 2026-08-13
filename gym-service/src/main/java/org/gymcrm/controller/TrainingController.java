package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.gymcrm.dto.request.AddTrainingRequest;
import org.gymcrm.exception.EntityNotFoundException;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.Training;
import org.gymcrm.service.TraineeService;
import org.gymcrm.service.TrainerService;
import org.gymcrm.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@Tag(name = "Trainings", description = "Training creation; listing is exposed under /trainees and /trainers")
public class TrainingController {

    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingMapper trainingMapper;

    public TrainingController(TrainingService trainingService, TraineeService traineeService,
                              TrainerService trainerService, TrainingMapper trainingMapper) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingMapper = trainingMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new training session", description = "Training type is inherited from the trainer's specialization")
    public void addTraining(@Valid @RequestBody AddTrainingRequest request) {
        Trainee trainee = traineeService.selectByUsername(request.traineeUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee with username " + request.traineeUsername() + " not found"));
        Trainer trainer = trainerService.selectByUsername(request.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer with username " + request.trainerUsername() + " not found"));

        Training training = trainingMapper.toEntity(request);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainer.getSpecialization());

        trainingService.create(training);
    }
}