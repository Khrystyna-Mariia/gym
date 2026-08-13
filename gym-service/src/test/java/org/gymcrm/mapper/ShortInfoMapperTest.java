package org.gymcrm.mapper;

import org.gymcrm.dto.response.TraineeShortInfo;
import org.gymcrm.dto.response.TrainerShortInfo;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShortInfoMapperTest {

    private final ShortInfoMapper mapper = Mappers.getMapper(ShortInfoMapper.class);

    @Test
    void toShortInfo_mapsTraineeFields() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        trainee.setUser(user);

        TraineeShortInfo shortInfo = mapper.toShortInfo(trainee);

        assertEquals("john.doe", shortInfo.username());
        assertEquals("John", shortInfo.firstName());
        assertEquals("Doe", shortInfo.lastName());
    }

    @Test
    void toShortInfo_handlesNullTraineeUser() {
        Trainee trainee = new Trainee();

        TraineeShortInfo shortInfo = mapper.toShortInfo(trainee);

        assertNull(shortInfo.username());
        assertNull(shortInfo.firstName());
    }

    @Test
    void toShortInfo_mapsTrainerSpecializationToString() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("anna.k");
        user.setFirstName("Anna");
        user.setLastName("K");
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.YOGA));

        TrainerShortInfo shortInfo = mapper.toShortInfo(trainer);

        assertEquals("anna.k", shortInfo.username());
        assertEquals("YOGA", shortInfo.specialization());
    }

    @Test
    void toTrainerShortInfoList_mapsListAndHandlesEmpty() {
        assertTrue(mapper.toTrainerShortInfoList(List.of()).isEmpty());

        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("anna.k");
        trainer.setUser(user);

        List<TrainerShortInfo> result = mapper.toTrainerShortInfoList(List.of(trainer));

        assertEquals(1, result.size());
        assertEquals("anna.k", result.get(0).username());
    }

    @Test
    void toTraineeShortInfoList_mapsListAndHandlesEmpty() {
        assertTrue(mapper.toTraineeShortInfoList(List.of()).isEmpty());

        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        trainee.setUser(user);

        List<TraineeShortInfo> result = mapper.toTraineeShortInfoList(List.of(trainee));

        assertEquals(1, result.size());
        assertEquals("john.doe", result.get(0).username());
    }
}