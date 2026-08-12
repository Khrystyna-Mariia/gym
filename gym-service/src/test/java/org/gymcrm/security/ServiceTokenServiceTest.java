package org.gymcrm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTokenServiceTest {

    private static final String SECRET = "1fZxu+Aap4l/FQda9j+AyFPZ4rhWEpSpsHshfliipgU=";
    private static final String SERVICE_NAME = "gym-service";
    private static final long EXPIRATION_MS = 60000;

    private ServiceTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new ServiceTokenService(SECRET, EXPIRATION_MS, SERVICE_NAME);
    }

    @Test
    void shouldGenerateValidJwtTokenWithCorrectClaims() {
        String token = tokenService.generateToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(SERVICE_NAME, claims.getSubject());
        assertEquals("service", claims.get("type"));
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
    }
}