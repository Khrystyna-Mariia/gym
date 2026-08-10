package org.gymcrm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptService {
    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private final int maxAttempts;
    private final Duration blockDuration;
    private final Clock clock;

    private final Map<String, Attempt> attemptsByUsername = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.login.max-attempts:3}") int maxAttempts,
            @Value("${security.login.lock-duration-minutes:5}") long lockDurationMinutes,
            Clock clock) {
        this.maxAttempts = maxAttempts;
        this.blockDuration = Duration.ofMinutes(lockDurationMinutes);
        this.clock = clock;
    }

    public boolean isBlocked(String username) {
        String key = normalize(username);
        Attempt attempt = attemptsByUsername.get(key);
        if (attempt == null) {
            return false;
        }

        if (attempt.blockedUntil != null) {
            if (Instant.now(clock).isBefore(attempt.blockedUntil)) {
                return true;
            }
            attemptsByUsername.remove(key);
        }
        return false;
    }

    public Duration remainingBlockDuration(String username) {
        Attempt attempt = attemptsByUsername.get(normalize(username));
        if (attempt == null || attempt.blockedUntil == null) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(Instant.now(clock), attempt.blockedUntil);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public void loginFailed(String username) {
        String key = normalize(username);

        attemptsByUsername.compute(key, (k, attempt) -> {
            if (attempt == null) {
                attempt = new Attempt();
            }

            if (attempt.blockedUntil != null && Instant.now(clock).isAfter(attempt.blockedUntil)) {
                attempt = new Attempt();
            }

            int count = attempt.failedCount.incrementAndGet();
            if (count >= maxAttempts) {
                attempt.blockedUntil = Instant.now(clock).plus(blockDuration);
                logger.warn("User '{}' blocked for {} minutes after {} failed login attempts",
                        username, blockDuration.toMinutes(), count);
            }
            return attempt;
        });
    }

    public void loginSucceeded(String username) {
        attemptsByUsername.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.toLowerCase();
    }

    private static class Attempt {
        private final AtomicInteger failedCount = new AtomicInteger(0);
        private volatile Instant blockedUntil;
    }
}