package org.gymcrm.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void isBlacklisted_returnsFalse_forUnknownToken() {
        TokenBlacklistService service = new TokenBlacklistService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        assertFalse(service.isBlacklisted("some-token"));
    }

    @Test
    void isBlacklisted_returnsTrue_afterBlacklisting() {
        TokenBlacklistService service = new TokenBlacklistService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.blacklist("token-123", FIXED_NOW.plus(Duration.ofHours(1)));

        assertTrue(service.isBlacklisted("token-123"));
    }

    @Test
    void isBlacklisted_doesNotAffectOtherTokens() {
        TokenBlacklistService service = new TokenBlacklistService(Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
        service.blacklist("token-123", FIXED_NOW.plus(Duration.ofHours(1)));

        assertFalse(service.isBlacklisted("token-456"));
    }

    @Test
    void isBlacklisted_returnsFalse_onceTokensOwnExpiryHasPassed() {
        MutableClock clock = new MutableClock(FIXED_NOW);
        TokenBlacklistService service = new TokenBlacklistService(clock);
        service.blacklist("token-123", FIXED_NOW.plus(Duration.ofMinutes(30)));

        assertTrue(service.isBlacklisted("token-123"));

        clock.advance(Duration.ofMinutes(31));
        assertFalse(service.isBlacklisted("token-123"), "entry should be evicted once the JWT itself would have expired");
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