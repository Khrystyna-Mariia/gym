package org.gymcrm.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class ServiceTokenService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String serviceName;

    public ServiceTokenService(@Value("${service.jwt.secret}") String secret,
                               @Value("${service.jwt.expiration-ms:60000}") long expirationMs,
                               @Value("${spring.application.name}") String serviceName) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.serviceName = serviceName;
    }

    public String generateToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(serviceName)
                .claim("type", "service")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}