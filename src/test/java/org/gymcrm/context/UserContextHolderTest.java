package org.gymcrm.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextHolderTest {

    @BeforeEach
    @AfterEach
    void cleanUp() {
        UserContextHolder.clear();
    }

    @Test
    void shouldReturnNullWhenNoCredentialsWereSet() {
        UserContextHolder.UserCredentials credentials = UserContextHolder.getCredentials();

        assertNull(credentials, "Credentials should be null initially");
    }

    @Test
    void shouldSetAndGetCredentialsSuccessfully() {
        String expectedUsername = "John.Smith";
        String expectedPassword = "securePassword123";

        UserContextHolder.setCredentials(expectedUsername, expectedPassword);
        UserContextHolder.UserCredentials actualCredentials = UserContextHolder.getCredentials();

        assertNotNull(actualCredentials);
        assertEquals(expectedUsername, actualCredentials.username());
        assertEquals(expectedPassword, actualCredentials.password());
    }

    @Test
    void shouldClearCredentialsSuccessfully() {
        UserContextHolder.setCredentials("tempUser", "tempPass");
        assertNotNull(UserContextHolder.getCredentials());

        UserContextHolder.clear();

        assertNull(UserContextHolder.getCredentials(), "Credentials should be null after clearing");
    }

}