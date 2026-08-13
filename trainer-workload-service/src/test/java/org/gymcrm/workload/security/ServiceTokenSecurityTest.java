package org.gymcrm.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceTokenSecurityTest {

    private final String secret = "1fZxu+Aap4l/FQda9j+AyFPZ4rhWEpSpsHshfliipgU=";
    private ServiceTokenValidator tokenValidator;
    private ServiceAuthenticationFilter securityFilter;

    @BeforeEach
    void setUp() {
        tokenValidator = new ServiceTokenValidator(secret);
        securityFilter = new ServiceAuthenticationFilter(tokenValidator);
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void tokenValidator_ValidToken_ReturnsTrue() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String validToken = Jwts.builder()
                .subject("gym-service")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        assertTrue(tokenValidator.isValid(validToken));
    }

    @Test
    void tokenValidator_InvalidSignature_ReturnsFalse() {
        SecretKey otherKey = Keys.hmacShaKeyFor("DifferentSecretKeyWithSufficientLength123456789!".getBytes(StandardCharsets.UTF_8));
        String invalidToken = Jwts.builder()
                .subject("gym-service")
                .signWith(otherKey)
                .compact();

        assertFalse(tokenValidator.isValid(invalidToken));
    }

    @Test
    void filter_WithValidBearerHeader_SetsAuthentication() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String validToken = Jwts.builder().subject("gym-service").signWith(key).compact();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        securityFilter.doFilterInternal(request, response, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("service", auth.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void filter_WithInvalidToken_DoesNotSetAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer not-a-real-token");

        securityFilter.doFilterInternal(request, response, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void filter_WithNoAuthorizationHeader_DoesNotSetAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(request, response);
    }
}