package org.gymcrm.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {

    private MeterRegistry registry;
    private GymMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new GymMetrics(registry);
    }

    @Test
    void incrementTraineeRegistrations_increasesOnlyTraineeCounter() {
        metrics.incrementTraineeRegistrations();
        metrics.incrementTraineeRegistrations();

        assertEquals(2.0, registry.get("gym.registrations").tag("role", "trainee").counter().count());
        assertEquals(0.0, registry.get("gym.registrations").tag("role", "trainer").counter().count());
    }

    @Test
    void incrementTrainerRegistrations_increasesOnlyTrainerCounter() {
        metrics.incrementTrainerRegistrations();

        assertEquals(1.0, registry.get("gym.registrations").tag("role", "trainer").counter().count());
        assertEquals(0.0, registry.get("gym.registrations").tag("role", "trainee").counter().count());
    }

    @Test
    void incrementLoginSuccess_increasesOnlySuccessCounter() {
        metrics.incrementLoginSuccess();

        assertEquals(1.0, registry.get("gym.login.attempts").tag("result", "success").counter().count());
        assertEquals(0.0, registry.get("gym.login.attempts").tag("result", "failure").counter().count());
    }

    @Test
    void incrementLoginFailure_increasesOnlyFailureCounter() {
        metrics.incrementLoginFailure();
        metrics.incrementLoginFailure();
        metrics.incrementLoginFailure();

        assertEquals(3.0, registry.get("gym.login.attempts").tag("result", "failure").counter().count());
    }

    @Test
    void incrementTrainingsCreated_increasesCounter() {
        metrics.incrementTrainingsCreated();

        assertEquals(1.0, registry.get("gym.trainings.created").counter().count());
    }
}