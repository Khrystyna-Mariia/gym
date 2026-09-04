package org.gymcrm.workload.cucumber;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TestServiceTokenFactory {

    private final SecretKey signingKey;

    public TestServiceTokenFactory(@Value("${service.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String validToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject("gym-service")
                .claim("type", "service")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();
    }

    public String invalidToken() {
        return "this.is.not-a-valid-jwt";
    }
}