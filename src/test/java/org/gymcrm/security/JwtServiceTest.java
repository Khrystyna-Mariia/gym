package org.gymcrm.security;

import org.gymcrm.model.Role;
import org.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long!!";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000L);
    }

    private UserPrincipal principal(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        return new UserPrincipal(user);
    }

    @Test
    void generateToken_producesTokenContainingCorrectUsername() {
        String token = jwtService.generateToken(principal("john.doe", Role.TRAINEE));

        assertNotNull(token);
        assertEquals("john.doe", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_returnsTrueForFreshTokenAndMatchingUsername() {
        String token = jwtService.generateToken(principal("john.doe", Role.TRAINEE));

        assertTrue(jwtService.isTokenValid(token, "john.doe"));
    }

    @Test
    void isTokenValid_returnsFalseWhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(principal("john.doe", Role.TRAINEE));

        assertFalse(jwtService.isTokenValid(token, "someone.else"));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() throws InterruptedException {
        JwtService shortLivedService = new JwtService(SECRET, 1L); // expires almost immediately
        String token = shortLivedService.generateToken(principal("john.doe", Role.TRAINEE));

        Thread.sleep(20);

        assertFalse(shortLivedService.isTokenValid(token, "john.doe"));
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        assertFalse(jwtService.isTokenValid("not-a-real-jwt-token", "john.doe"));
    }

    @Test
    void isTokenValid_returnsFalseWhenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService("a-completely-different-secret-key-32-bytes+", 3600000L);
        String token = otherService.generateToken(principal("john.doe", Role.TRAINEE));

        assertFalse(jwtService.isTokenValid(token, "john.doe"));
    }

    @Test
    void extractUsername_throwsForInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("garbage"));
    }
}