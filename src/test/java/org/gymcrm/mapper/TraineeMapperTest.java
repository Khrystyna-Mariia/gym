package org.gymcrm.mapper;

import org.gymcrm.dto.request.TraineeRegistrationRequest;
import org.gymcrm.dto.request.UpdateTraineeProfileRequest;
import org.gymcrm.dto.response.RegistrationResponse;
import org.gymcrm.dto.response.TraineeProfileResponse;
import org.gymcrm.dto.response.TraineeShortInfo;
import org.gymcrm.dto.response.UpdateTraineeProfileResponse;
import org.gymcrm.model.Trainee;
import org.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TraineeMapperTest {

    private final TraineeMapper mapper = Mappers.getMapper(TraineeMapper.class);

    @Test
    void toEntity_mapsFlatRequestFieldsIntoNestedUser() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(2000, 5, 14), "Main St 1");

        Trainee result = mapper.toEntity(request);

        assertNotNull(result.getUser());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertNull(result.getId());
        assertTrue(result.getTrainers().isEmpty());
        assertTrue(result.getTrainings().isEmpty());
    }

    @Test
    void toEntity_returnsNullWhenRequestIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void updateEntityFromRequest_mutatesExistingTraineeInPlace() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User());
        trainee.getUser().setUsername("john.doe");

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "john.doe", "Johnny", "Doey", LocalDate.of(1999, 1, 1), "New Addr", false);

        mapper.updateEntityFromRequest(request, trainee);

        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("Doey", trainee.getUser().getLastName());
        assertFalse(trainee.getUser().isActive());
        assertEquals("john.doe", trainee.getUser().getUsername());
    }

    @Test
    void toRegistrationResponse_extractsUsernameAndPasswordFromUser() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("genPass123");
        trainee.setUser(user);

        RegistrationResponse response = mapper.toRegistrationResponse(trainee);

        assertEquals("john.doe", response.username());
        assertEquals("genPass123", response.password());
    }

    @Test
    void toShortInfo_mapsUserFieldsFlattened() {
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
    void toProfileResponse_mapsAllFieldsAndEmptyTrainersList() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 5, 14));
        trainee.setAddress("Main St 1");

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertTrue(response.isActive());
        assertEquals(LocalDate.of(2000, 5, 14), response.dateOfBirth());
        assertEquals("Main St 1", response.address());
        assertNotNull(response.trainers());
        assertTrue(response.trainers().isEmpty());
    }

    @Test
    void toUpdateResponse_includesUsernameOnTopOfProfileFields() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        trainee.setUser(user);

        UpdateTraineeProfileResponse response = mapper.toUpdateResponse(trainee);

        assertEquals("john.doe", response.username());
        assertEquals("John", response.firstName());
        assertTrue(response.isActive());
    }

    @Test
    void toProfileResponse_handlesNullUserGracefully() {
        Trainee trainee = new Trainee();

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertNull(response.firstName());
        assertNull(response.lastName());
        assertFalse(response.isActive());
    }

    @Test
    void toShortInfo_handlesNullUserGracefully() {
        Trainee trainee = new Trainee();

        TraineeShortInfo shortInfo = mapper.toShortInfo(trainee);

        assertNull(shortInfo.username());
        assertNull(shortInfo.firstName());
    }
}