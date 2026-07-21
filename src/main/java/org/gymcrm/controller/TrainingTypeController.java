package org.gymcrm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gymcrm.dto.response.TrainingTypeResponse;
import org.gymcrm.mapper.TrainingMapper;
import org.gymcrm.service.TrainingTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@Tag(name = "Training Types", description = "Read-only reference data for training specializations")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final TrainingMapper trainingMapper;

    public TrainingTypeController(TrainingTypeService trainingTypeService, TrainingMapper trainingMapper) {
        this.trainingTypeService = trainingTypeService;
        this.trainingMapper = trainingMapper;
    }

    @GetMapping
    @Operation(summary = "Get all training types",
            description = "Returns the constant list of available training types (specializations)")
    @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingMapper.toTrainingTypeResponseList(trainingTypeService.selectAll());
    }
}