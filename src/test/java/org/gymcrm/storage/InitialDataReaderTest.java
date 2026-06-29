package org.gymcrm.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitialDataReaderTest {
    private InitialDataReader reader;

    @BeforeEach
    void setUp() {
        reader = new InitialDataReader();
    }

    @Test
    void shouldReadNonEmptyAndNonCommentLines() {
        String fileContent = """
                # Initial data comment
                
                TRAINING_TYPE;1;Fitness
                   
                TRAINEE;1;Alex;Gomez;Alex.Gomez;password123;true;2000-05-15;Kyiv
                # Another comment
                TRAINER;1;David;Miller;David.Miller;password789;true;1
                """;

        Resource resource = new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));

        List<String> lines = reader.readLines(resource);

        assertEquals(3, lines.size());
        assertEquals("TRAINING_TYPE;1;Fitness", lines.get(0));
        assertEquals("TRAINEE;1;Alex;Gomez;Alex.Gomez;password123;true;2000-05-15;Kyiv", lines.get(1));
        assertEquals("TRAINER;1;David;Miller;David.Miller;password789;true;1", lines.get(2));
    }

    @Test
    void shouldReturnEmptyListWhenFileContainsOnlyCommentsAndBlankLines() {
        String fileContent = """
                # Comment one
                
                   
                # Comment two
                """;

        Resource resource = new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));

        List<String> lines = reader.readLines(resource);

        assertTrue(lines.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenResourceIsNull() {
        assertThrows(IllegalStateException.class, () -> reader.readLines(null));
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {
        Resource resource = new ClassPathResource("not-existing-initial-data.txt");

        assertThrows(IllegalStateException.class, () -> reader.readLines(resource));
    }
}