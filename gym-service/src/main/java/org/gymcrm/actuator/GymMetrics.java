package org.gymcrm.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter loginSuccess;
    private final Counter loginFailure;
    private final Counter trainingsCreated;

    public GymMetrics(MeterRegistry registry) {
        this.traineeRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainee")
                .description("Number of trainee registrations")
                .register(registry);

        this.trainerRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainer")
                .description("Number of trainer registrations")
                .register(registry);

        this.loginSuccess = Counter.builder("gym.login.attempts")
                .tag("result", "success")
                .description("Number of successful login attempts")
                .register(registry);

        this.loginFailure = Counter.builder("gym.login.attempts")
                .tag("result", "failure")
                .description("Number of failed login attempts")
                .register(registry);

        this.trainingsCreated = Counter.builder("gym.trainings.created")
                .description("Number of training sessions created")
                .register(registry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccess.increment();
    }

    public void incrementLoginFailure() {
        loginFailure.increment();
    }

    public void incrementTrainingsCreated() {
        trainingsCreated.increment();
    }
}