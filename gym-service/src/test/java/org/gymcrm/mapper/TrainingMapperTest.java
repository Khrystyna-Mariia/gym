package org.gymcrm.mapper;

import org.gymcrm.dto.request.AddTrainingRequest;
import org.gymcrm.dto.response.TraineeTrainingResponse;
import org.gymcrm.dto.response.TrainerTrainingResponse;
import org.gymcrm.dto.response.TrainingTypeResponse;
import org.gymcrm.model.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainingMapperTest {

    private final TrainingMapper mapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void toEntity_mapsScalarFieldsAndIgnoresRelations() {
        AddTrainingRequest request = new AddTrainingRequest(
                "john.doe", "anna.k", "Morning Yoga", LocalDate.of(2026, 8, 1), 60);

        Training result = mapper.toEntity(request);

        assertEquals("Morning Yoga", result.getTrainingName());
        assertEquals(LocalDate.of(2026, 8, 1), result.getTrainingDate());
        assertEquals(60, result.getTrainingDuration());
        assertNull(result.getTrainee());
        assertNull(result.getTrainer());
        assertNull(result.getTrainingType());
        assertNull(result.getId());
    }

    @Test
    void toTraineeTrainingResponse_buildsTrainerFullNameAndTypeString() {
        Training training = buildTraining();

        TraineeTrainingResponse response = mapper.toTraineeTrainingResponse(training);

        assertEquals("Morning Yoga", response.trainingName());
        assertEquals("YOGA", response.trainingType());
        assertEquals("Anna K", response.trainerName());
        assertEquals(60, response.trainingDuration());
    }

    @Test
    void toTrainerTrainingResponse_buildsTraineeFullNameAndTypeString() {
        Training training = buildTraining();

        TrainerTrainingResponse response = mapper.toTrainerTrainingResponse(training);

        assertEquals("Morning Yoga", response.trainingName());
        assertEquals("YOGA", response.trainingType());
        assertEquals("John Doe", response.traineeName());
    }

    @Test
    void toTraineeTrainingResponseList_mapsEachElement() {
        List<TraineeTrainingResponse> result = mapper.toTraineeTrainingResponseList(List.of(buildTraining()));
        assertEquals(1, result.size());
        assertEquals("Anna K", result.get(0).trainerName());
    }

    @Test
    void toTrainingTypeResponse_mapsIdAndEnumName() {
        TrainingType type = new TrainingType(3L, TrainingTypeEnum.STRENGTH);

        TrainingTypeResponse response = mapper.toTrainingTypeResponse(type);

        assertEquals("STRENGTH", response.trainingType());
        assertEquals(3L, response.trainingTypeId());
    }

    @Test
    void toTrainingTypeResponseList_returnsEmptyForEmptyInput() {
        assertTrue(mapper.toTrainingTypeResponseList(List.of()).isEmpty());
    }

    @Test
    void fullName_returnsNullWhenUserIsNull() {
        assertNull(mapper.fullName(null));
    }

    @Test
    void fullName_concatenatesFirstAndLastName() {
        User user = new User();
        user.setFirstName("Anna");
        user.setLastName("K");
        assertEquals("Anna K", mapper.fullName(user));
    }

    @Test
    void toTrainerTrainingResponseList_mapsEachElement() {
        List<TrainerTrainingResponse> result = mapper.toTrainerTrainingResponseList(List.of(buildTraining()));
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).traineeName());
    }

    private Training buildTraining() {
        User trainerUser = new User();
        trainerUser.setFirstName("Anna");
        trainerUser.setLastName("K");
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        User traineeUser = new User();
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        Training training = new Training();
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(LocalDate.of(2026, 8, 1));
        training.setTrainingDuration(60);
        training.setTrainingType(new TrainingType(1L, TrainingTypeEnum.YOGA));
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        return training;
    }

    @Test
    void toTraineeTrainingResponse_handlesNullTrainingType() {
        Training training = buildTraining();
        training.setTrainingType(null);

        TraineeTrainingResponse response = mapper.toTraineeTrainingResponse(training);

        assertNull(response.trainingType());
        assertEquals("Anna K", response.trainerName());
    }

}