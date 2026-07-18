package org.gymcrm.mapper;

import org.gymcrm.dto.request.TraineeRegistrationRequest;
import org.gymcrm.dto.request.UpdateTraineeProfileRequest;
import org.gymcrm.dto.response.*;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TraineeMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Trainee toEntity(TraineeRegistrationRequest request);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "isActive")
    void updateEntityFromRequest(UpdateTraineeProfileRequest request, @MappingTarget Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    RegistrationResponse toRegistrationResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeShortInfo toShortInfo(Trainee trainee);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.active")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.active")
    UpdateTraineeProfileResponse toUpdateResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerShortInfo toTrainerShortInfo(Trainer trainer);

    List<TrainerShortInfo> toTrainerShortInfoList(Set<Trainer> trainers);

    default String mapTrainingTypeToString(TrainingType value) {
        return value == null || value.getTrainingTypeName() == null ? null : value.getTrainingTypeName().name();
    }
}