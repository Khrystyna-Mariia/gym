package org.gymcrm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {
    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
    }

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUsernameDoesNotExist(){
        Set<String> existingUsernames = Set.of("Anna.Brown", "Michael.Green");

        String result = usernameGenerator.generate("John", "Smith", existingUsernames::contains);

        assertEquals("John.Smith", result);
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenBaseUsernameAlreadyExists() {
        Set<String> existingUsernames = Set.of("John.Smith");

        String result = usernameGenerator.generate("John", "Smith", existingUsernames::contains);

        assertEquals("John.Smith1", result);
    }

    @Test
    void shouldGenerateNextAvailableSuffixWhenSeveralUsernamesAlreadyExist() {
        Set<String> existingUsernames = Set.of(
                "John.Smith",
                "John.Smith1",
                "John.Smith2"
        );

        String result = usernameGenerator.generate("John", "Smith", existingUsernames::contains);

        assertEquals("John.Smith3", result);
    }
}