package org.gymcrm.mapper;

import org.gymcrm.dto.response.TraineeShortInfo;
import org.gymcrm.dto.response.TrainerShortInfo;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShortInfoMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerShortInfo toShortInfo(Trainer trainer);

    List<TrainerShortInfo> toTrainerShortInfoList(List<Trainer> trainers);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeShortInfo toShortInfo(Trainee trainee);

    List<TraineeShortInfo> toTraineeShortInfoList(List<Trainee> trainees);
}