package org.gymcrm.mapper;

import org.gymcrm.dto.request.TraineeRegistrationRequest;
import org.gymcrm.dto.request.UpdateTraineeProfileRequest;
import org.gymcrm.dto.response.TraineeProfileResponse;
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
    void toEntity_mapsFlatRequestFieldsIntoNestedUserAndEntity() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(2000, 5, 14), "Main St 1");

        Trainee result = mapper.toEntity(request);

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertEquals(LocalDate.of(2000, 5, 14), result.getDateOfBirth());
        assertEquals("Main St 1", result.getAddress());
        assertNull(result.getId());
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
    void toProfileResponse_mapsAllFieldsCorrectly() {
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 5, 14));
        trainee.setAddress("Main St 1");

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertTrue(response.isActive());
        assertEquals(LocalDate.of(2000, 5, 14), response.dateOfBirth());
        assertEquals("Main St 1", response.address());
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

        assertNotNull(response);
        assertEquals("john.doe", response.username());
        assertEquals("John", response.firstName());
        assertTrue(response.isActive());
    }

    @Test
    void toProfileResponse_handlesNullUserGracefully() {
        Trainee trainee = new Trainee();

        TraineeProfileResponse response = mapper.toProfileResponse(trainee);

        assertNotNull(response);
        assertNull(response.firstName());
        assertNull(response.lastName());
        assertFalse(response.isActive());
    }
}