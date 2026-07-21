package org.gymcrm.mapper;

import org.gymcrm.dto.request.TrainerRegistrationRequest;
import org.gymcrm.dto.request.UpdateTrainerProfileRequest;
import org.gymcrm.dto.response.RegistrationResponse;
import org.gymcrm.dto.response.TrainerProfileResponse;
import org.gymcrm.dto.response.TrainerShortInfo;
import org.gymcrm.dto.response.UpdateTrainerProfileResponse;
import org.gymcrm.model.Trainer;
import org.gymcrm.model.TrainingType;
import org.gymcrm.model.TrainingTypeEnum;
import org.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainerMapperTest {

    private final TrainerMapper mapper = Mappers.getMapper(TrainerMapper.class);

    @Test
    void toEntity_mapsFlatRequestFieldsAndIgnoresSpecialization() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Anna", "K", 1L);

        Trainer result = mapper.toEntity(request);

        assertEquals("Anna", result.getUser().getFirstName());
        assertEquals("K", result.getUser().getLastName());
        assertNull(result.getSpecialization());
        assertNull(result.getId());
        assertTrue(result.getTrainees().isEmpty());
    }

    @Test
    void updateEntityFromRequest_neverTouchesSpecialization() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User());
        TrainingType original = new TrainingType(1L, TrainingTypeEnum.YOGA);
        trainer.setSpecialization(original);

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest("anna.k", "Anna", "Kova", true);
        mapper.updateEntityFromRequest(request, trainer);

        assertEquals("Anna", trainer.getUser().getFirstName());
        assertEquals("Kova", trainer.getUser().getLastName());
        assertTrue(trainer.getUser().isActive());
        assertSame(original, trainer.getSpecialization(), "Specialization must remain untouched (read-only field)");
    }

    @Test
    void toRegistrationResponse_extractsCredentials() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("anna.k");
        user.setPassword("genPass456");
        trainer.setUser(user);

        RegistrationResponse response = mapper.toRegistrationResponse(trainer);

        assertEquals("anna.k", response.username());
        assertEquals("genPass456", response.password());
    }

    @Test
    void toShortInfo_mapsSpecializationEnumToString() {
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
    void toProfileResponse_mapsAllFieldsWithEmptyTraineesList() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setFirstName("Anna");
        user.setLastName("K");
        user.setActive(true);
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.FITNESS));

        TrainerProfileResponse response = mapper.toProfileResponse(trainer);

        assertEquals("Anna", response.firstName());
        assertEquals("FITNESS", response.specialization());
        assertTrue(response.isActive());
        assertNotNull(response.trainees());
        assertTrue(response.trainees().isEmpty());
    }

    @Test
    void toUpdateResponse_includesUsernameOnTopOfProfileFields() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("anna.k");
        user.setFirstName("Anna");
        user.setLastName("K");
        user.setActive(true);
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.ZUMBA));

        UpdateTrainerProfileResponse response = mapper.toUpdateResponse(trainer);

        assertEquals("anna.k", response.username());
        assertEquals("ZUMBA", response.specialization());
    }

    @Test
    void toShortInfoList_mapsEachTrainerAndReturnsEmptyForEmptyInput() {
        assertTrue(mapper.toShortInfoList(List.of()).isEmpty());

        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("anna.k");
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(1L, TrainingTypeEnum.CARDIO));

        List<TrainerShortInfo> result = mapper.toShortInfoList(List.of(trainer));

        assertEquals(1, result.size());
        assertEquals("anna.k", result.get(0).username());
    }

    @Test
    void toProfileResponse_handlesNullUserAndNullSpecialization() {
        Trainer trainer = new Trainer();

        TrainerProfileResponse response = mapper.toProfileResponse(trainer);

        assertNull(response.firstName());
        assertNull(response.specialization());
        assertFalse(response.isActive());
    }

    @Test
    void toShortInfoList_handlesTrainerWithNullSpecialization() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User());

        List<TrainerShortInfo> result = mapper.toShortInfoList(List.of(trainer));

        assertNull(result.get(0).specialization());
    }
}