package org.gymcrm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private final Clock clock;

    public TokenBlacklistService(Clock clock) {
        this.clock = clock;
    }

    public void blacklist(String token, Instant tokenExpiresAt) {
        cleanupExpiredEntries();
        blacklistedTokens.put(token, tokenExpiresAt);
        logger.debug("Token blacklisted, will be evicted at {}", tokenExpiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant expiresAt = blacklistedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (Instant.now(clock).isAfter(expiresAt)) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void cleanupExpiredEntries() {
        Instant now = Instant.now(clock);
        blacklistedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}