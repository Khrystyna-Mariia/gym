package org.gymcrm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger("REST_CALL");
    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logCall(wrappedRequest, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String requestBody = extractPayload(request.getContentAsByteArray());
        String responseBody = extractPayload(response.getContentAsByteArray());
        int status = response.getStatus();

        if (status >= 400) {
            logger.warn("{} {} | status={} | request={} | response={}",
                    request.getMethod(), request.getRequestURI(), status, requestBody, responseBody);
        } else {
            logger.info("{} {} | status={} | request={}",
                    request.getMethod(), request.getRequestURI(), status, requestBody);
        }
    }

    private String extractPayload(byte[] content) {
        if (content == null || content.length == 0) {
            return "-";
        }
        int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);
        return new String(content, 0, length, StandardCharsets.UTF_8);
    }
}