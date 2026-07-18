package org.gymcrm.mapper;

import org.gymcrm.dto.request.AddTrainingRequest;
import org.gymcrm.dto.response.TraineeTrainingResponse;
import org.gymcrm.dto.response.TrainerTrainingResponse;
import org.gymcrm.dto.response.TrainingTypeResponse;
import org.gymcrm.model.Training;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingMapper {

    Training toEntity(AddTrainingRequest request);

    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainerName", source = "trainer.user", qualifiedByName = "fullName")
    TraineeTrainingResponse toTraineeTrainingResponse(Training training);

    List<TraineeTrainingResponse> toTraineeTrainingResponseList(List<Training> trainings);

    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "traineeName", source = "trainee.user", qualifiedByName = "fullName")
    TrainerTrainingResponse toTrainerTrainingResponse(Training training);

    List<TrainerTrainingResponse> toTrainerTrainingResponseList(List<Training> trainings);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "trainingTypeId", source = "id")
    TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toTrainingTypeResponseList(List<TrainingType> trainingTypes);

    @Named("fullName")
    default String fullName(User user) {
        return user == null ? null : user.getFirstName() + " " + user.getLastName();
    }
}