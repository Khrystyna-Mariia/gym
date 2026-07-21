package org.gymcrm.filter;

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
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/training-types");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.setStatus(200);
            httpResp.getWriter().write("[{\"trainingType\":\"YOGA\"}]");
        };

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals("[{\"trainingType\":\"YOGA\"}]", response.getContentAsString());
    }

    @Test
    void requestBody_isCachedAndReadableAfterChainExecution() {
        String payload = "{\"username\":\"john.doe\"}";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/trainees");
        request.setContent(payload.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, resp) -> {
            req.getInputStream().readAllBytes();
            ((HttpServletResponse) resp).setStatus(201);
        };

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        assertEquals(201, response.getStatus());
    }

    @Test
    void errorResponse_isStillCopiedBackToClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/trainees/ghost");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> {
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            httpResp.setStatus(404);
            httpResp.getWriter().write("{\"message\":\"not found\"}");
        };

        filter.doFilter(request, response, chain);

        assertEquals(404, response.getStatus());
        assertEquals("{\"message\":\"not found\"}", response.getContentAsString());
    }

    @Test
    void emptyBody_doesNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/training-types");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> ((HttpServletResponse) resp).setStatus(200);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
    }
}