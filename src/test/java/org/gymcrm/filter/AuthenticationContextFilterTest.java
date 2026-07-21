package org.gymcrm.filter;

import jakarta.servlet.FilterChain;
import org.gymcrm.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationContextFilterTest {

    private final AuthenticationContextFilter filter = new AuthenticationContextFilter();

    @AfterEach
    void cleanup() {
        UserContextHolder.clear();
    }

    @Test
    void validBasicAuthHeader_populatesCredentialsDuringChainExecution() throws Exception {
        String encoded = Base64.getEncoder().encodeToString("john.doe:pass123".getBytes());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic " + encoded);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AtomicReference<UserContextHolder.UserCredentials> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(UserContextHolder.getCredentials());
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertNotNull(captured.get());
        assertEquals("john.doe", captured.get().username());
        assertEquals("pass123", captured.get().password());
        assertNull(UserContextHolder.getCredentials(), "Context must be cleared after the request completes");
    }

    @Test
    void noAuthorizationHeader_credentialsStayNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AtomicReference<UserContextHolder.UserCredentials> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(UserContextHolder.getCredentials());
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertNull(captured.get());
    }

    @Test
    void nonBasicAuthorizationHeader_isIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(UserContextHolder.getCredentials());
    }

    @Test
    void malformedBase64_doesNotThrowAndContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic %%not-valid-base64%%");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        verify(chain).doFilter(request, response);
    }

    @Test
    void decodedHeaderWithoutColon_isIgnoredAsMalformed() throws Exception {
        String encoded = Base64.getEncoder().encodeToString("no-colon-here".getBytes());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic " + encoded);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AtomicReference<UserContextHolder.UserCredentials> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(UserContextHolder.getCredentials());
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertNull(captured.get());
    }

    @Test
    void contextIsClearedEvenWhenChainThrows() throws Exception {
        String encoded = Base64.getEncoder().encodeToString("john.doe:pass123".getBytes());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic " + encoded);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("downstream failure")).when(chain).doFilter(any(), any());

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
        assertNull(UserContextHolder.getCredentials(), "finally block must clear context even on exception");
    }
}