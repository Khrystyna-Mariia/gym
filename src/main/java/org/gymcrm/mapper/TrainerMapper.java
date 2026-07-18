package org.gymcrm.mapper;

import org.gymcrm.dto.request.TrainerRegistrationRequest;
import org.gymcrm.dto.request.UpdateTrainerProfileRequest;
import org.gymcrm.dto.response.*;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainerMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Trainer toEntity(TrainerRegistrationRequest request);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "isActive")
    void updateEntityFromRequest(UpdateTrainerProfileRequest request, @MappingTarget Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    RegistrationResponse toRegistrationResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerShortInfo toShortInfo(Trainer trainer);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.active")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.active")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    UpdateTrainerProfileResponse toUpdateResponse(Trainer trainer);

    List<TrainerShortInfo> toShortInfoList(List<Trainer> trainers);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeShortInfo toTraineeShortInfo(Trainee trainee);

    List<TraineeShortInfo> toTraineeShortInfoList(Set<Trainee> trainees);

    default String mapTrainingTypeToString(TrainingType value) {
        return value == null || value.getTrainingTypeName() == null ? null : value.getTrainingTypeName().name();
    }
}