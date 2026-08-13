package org.gymcrm.workload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RestCallLoggingFilterTest {

    private final RestCallLoggingFilter filter = new RestCallLoggingFilter();

    @Test
    void successfulCall_copiesResponseBodyBackToClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workload/david.miller");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.setStatus(200);
            httpResp.getWriter().write("{\"username\":\"david.miller\"}");
        };

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals("{\"username\":\"david.miller\"}", response.getContentAsString());
    }

    @Test
    void requestBody_isCachedAndReadableAfterChainExecution() {
        String payload = "{\"trainerUsername\":\"david.miller\",\"actionType\":\"ADD\"}";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/workload");
        request.setContent(payload.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, resp) -> {
            req.getInputStream().readAllBytes();
            ((HttpServletResponse) resp).setStatus(200);
        };

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        assertEquals(200, response.getStatus());
    }

    @Test
    void errorResponse_isStillCopiedBackToClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workload/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.setStatus(404);
            httpResp.getWriter().write("{\"message\":\"Trainer not found\"}");
        };

        filter.doFilter(request, response, chain);

        assertEquals(404, response.getStatus());
        assertEquals("{\"message\":\"Trainer not found\"}", response.getContentAsString());
    }

    @Test
    void emptyBody_doesNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workload/david.miller");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> ((HttpServletResponse) resp).setStatus(200);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
    }

    @Test
    void longPayload_isTruncatedWithoutThrowing() {
        String longPayload = "a".repeat(1200);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/workload");
        request.setContent(longPayload.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, resp) -> ((HttpServletResponse) resp).setStatus(200);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        assertEquals(200, response.getStatus());
    }
}