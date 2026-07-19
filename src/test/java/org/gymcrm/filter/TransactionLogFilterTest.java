package org.gymcrm.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionLogFilterTest {

    private final TransactionLogFilter filter = new TransactionLogFilter();

    @Test
    void generatesTransactionIdInMdcDuringChainAndClearsAfterward() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AtomicReference<String> capturedTxId = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedTxId.set(MDC.get(TransactionLogFilter.TRANSACTION_ID_KEY));
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertNotNull(capturedTxId.get());
        assertNull(MDC.get(TransactionLogFilter.TRANSACTION_ID_KEY), "MDC must be cleared after the request");
    }

    @Test
    void setsTransactionIdResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader(TransactionLogFilter.TRANSACTION_ID_HEADER));
    }

    @Test
    void eachRequestGetsAUniqueTransactionId() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), first, chain);
        filter.doFilter(new MockHttpServletRequest(), second, chain);

        assertNotEquals(
                first.getHeader(TransactionLogFilter.TRANSACTION_ID_HEADER),
                second.getHeader(TransactionLogFilter.TRANSACTION_ID_HEADER));
    }

    @Test
    void mdcIsClearedEvenWhenChainThrows() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("downstream failure")).when(chain).doFilter(any(), any());

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
        assertNull(MDC.get(TransactionLogFilter.TRANSACTION_ID_KEY));
    }
}