package org.gymcrm.util;

import org.gymcrm.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator(10);
    }

    @Test
    void shouldGeneratePasswordWithConfiguredLength() {
        String password = passwordGenerator.generate();

        assertEquals(10, password.length());
    }

    @Test
    void shouldGeneratePasswordOnlyWithLettersAndDigits() {
        String password = passwordGenerator.generate();

        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void shouldGeneratePasswordWithDifferentConfiguredLength() {
        PasswordGenerator twelveCharacterPasswordGenerator = new PasswordGenerator(12);

        String password = twelveCharacterPasswordGenerator.generate();

        assertEquals(12, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{12}"));
    }

    @Test
    void shouldThrowExceptionWhenPasswordLengthIsZero() {
        assertThrows(ValidationException.class, () -> new PasswordGenerator(0));
    }

    @Test
    void shouldThrowExceptionWhenPasswordLengthIsNegative() {
        assertThrows(ValidationException.class, () -> new PasswordGenerator(-1));
    }
}