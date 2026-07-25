package org.gymcrm.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitialDataFileHealthIndicatorTest {

    @Test
    void health_returnsUpWhenFileExistsAndReadable() {
        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator();
        Resource existingResource = new ClassPathResource("initial-data.txt");
        ReflectionTestUtils.setField(indicator, "initialDataFile", existingResource);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("initial-data.txt", health.getDetails().get("file"));
    }

    @Test
    void health_returnsDownWhenFileDoesNotExist() {
        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator();
        Resource missingResource = new ClassPathResource("this-file-does-not-exist.txt");
        ReflectionTestUtils.setField(indicator, "initialDataFile", missingResource);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Initial data file is missing or unreadable", health.getDetails().get("error"));
    }

    @Test
    void health_returnsDownWhenResourceIsNull() {
        InitialDataFileHealthIndicator indicator = new InitialDataFileHealthIndicator();
        ReflectionTestUtils.setField(indicator, "initialDataFile", null);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("unknown", health.getDetails().get("file"));
    }
}