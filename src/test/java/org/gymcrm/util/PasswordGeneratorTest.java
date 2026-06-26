package org.gymcrm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void shouldGeneratePasswordWithLengthTen() {
        String password = passwordGenerator.generate();

        assertEquals(10, password.length());
    }

    @Test
    void shouldGeneratePasswordOnlyWithLettersAndDigits() {
        String password = passwordGenerator.generate();

        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }
}