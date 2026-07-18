package org.gymcrm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gymcrm.context.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthenticationContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationContextFilter.class);
    private static final String BASIC_PREFIX = "Basic ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            extractCredentials(request);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private void extractCredentials(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            return;
        }
        try {
            String decoded = new String(
                    Base64.getDecoder().decode(header.substring(BASIC_PREFIX.length())),
                    StandardCharsets.UTF_8
            );
            int separator = decoded.indexOf(':');
            if (separator < 0) {
                logger.warn("Malformed Authorization header received");
                return;
            }
            UserContextHolder.setCredentials(decoded.substring(0, separator), decoded.substring(separator + 1));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to decode Authorization header: {}", e.getMessage());
        }
    }
}