package org.gymcrm.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void isBlocked_returnsFalseInitially() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        assertFalse(service.isBlocked("john.doe"));
    }

    @Test
    void isBlocked_returnsFalseAfterTwoFailures() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        assertFalse(service.isBlocked("john.doe"));
    }

    @Test
    void isBlocked_returnsTrueAfterThirdFailure() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        assertTrue(service.isBlocked("john.doe"));
    }

    @Test
    void isBlocked_isCaseInsensitiveAndPerUsername() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.loginFailed("John.Doe");
        service.loginFailed("john.doe");
        service.loginFailed("JOHN.DOE");

        assertTrue(service.isBlocked("john.doe"));
        assertFalse(service.isBlocked("anna.k"), "A different user must not be affected");
    }

    @Test
    void isBlocked_returnsFalseAfterBlockDurationElapses() {
        MutableClock clock = new MutableClock(FIXED_NOW);
        LoginAttemptService service = new LoginAttemptService(3, 5, clock);

        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        assertTrue(service.isBlocked("john.doe"));

        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertFalse(service.isBlocked("john.doe"));
    }

    @Test
    void loginFailed_resetsCounterAfterBlockExpires() {
        MutableClock clock = new MutableClock(FIXED_NOW);
        LoginAttemptService service = new LoginAttemptService(3, 5, clock);

        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        assertTrue(service.isBlocked("john.doe"));

        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        service.loginFailed("john.doe");

        assertFalse(service.isBlocked("john.doe"), "A single failure after expiry must not immediately re-block");
    }

    @Test
    void loginSucceeded_clearsFailureCount() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        service.loginSucceeded("john.doe");
        service.loginFailed("john.doe");

        assertFalse(service.isBlocked("john.doe"), "Failure count should have reset after a successful login");
    }

    @Test
    void remainingBlockDuration_returnsZeroWhenNotBlocked() {
        LoginAttemptService service = new LoginAttemptService(3, 5, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        assertEquals(Duration.ZERO, service.remainingBlockDuration("john.doe"));
    }

    @Test
    void remainingBlockDuration_decreasesAsClockAdvances() {
        MutableClock clock = new MutableClock(FIXED_NOW);
        LoginAttemptService service = new LoginAttemptService(3, 5, clock);

        service.loginFailed("john.doe");
        service.loginFailed("john.doe");
        service.loginFailed("john.doe");

        Duration remainingAtStart = service.remainingBlockDuration("john.doe");
        assertEquals(5, remainingAtStart.toMinutes());

        clock.advance(Duration.ofMinutes(2));
        Duration remainingAfterTwoMinutes = service.remainingBlockDuration("john.doe");
        assertEquals(3, remainingAfterTwoMinutes.toMinutes());
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { this.instant = this.instant.plus(duration); }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}