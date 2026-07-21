package org.gymcrm.mapper;

import org.gymcrm.dto.request.TraineeRegistrationRequest;
import org.gymcrm.dto.request.UpdateTraineeProfileRequest;
import org.gymcrm.dto.response.RegistrationResponse;
import org.gymcrm.dto.response.TraineeProfileResponse;
import org.gymcrm.dto.response.TraineeShortInfo;
import org.gymcrm.dto.response.UpdateTraineeProfileResponse;
import org.gymcrm.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = TrainerMapper.class)
public interface TraineeMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(TraineeRegistrationRequest request);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "isActive")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
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
}