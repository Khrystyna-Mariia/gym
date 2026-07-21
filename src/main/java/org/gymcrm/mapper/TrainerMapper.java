package org.gymcrm.mapper;

import org.gymcrm.dto.request.TrainerRegistrationRequest;
import org.gymcrm.dto.request.UpdateTrainerProfileRequest;
import org.gymcrm.dto.response.RegistrationResponse;
import org.gymcrm.dto.response.TrainerProfileResponse;
import org.gymcrm.dto.response.TrainerShortInfo;
import org.gymcrm.dto.response.UpdateTrainerProfileResponse;
import org.gymcrm.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = TraineeMapper.class)
public interface TrainerMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toEntity(TrainerRegistrationRequest request);

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "isActive")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
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
}