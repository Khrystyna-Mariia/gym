package org.gymcrm.client;

import feign.RequestTemplate;
import org.gymcrm.filter.TransactionLogFilter;
import org.gymcrm.security.ServiceTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignClientInterceptorTest {

    @Mock
    private ServiceTokenService serviceTokenService;

    private FeignClientInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FeignClientInterceptor(serviceTokenService);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void apply_shouldAddAuthHeaderAndTransactionIdHeader_whenTransactionIdPresent() {
        when(serviceTokenService.generateToken()).thenReturn("mocked-service-jwt");
        MDC.put(TransactionLogFilter.TRANSACTION_ID_KEY, "tx-99999");

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertEquals("Bearer mocked-service-jwt", template.headers().get("Authorization").iterator().next());

        assertTrue(template.headers().containsKey(TransactionLogFilter.TRANSACTION_ID_HEADER));
        assertEquals("tx-99999", template.headers().get(TransactionLogFilter.TRANSACTION_ID_HEADER).iterator().next());
    }

    @Test
    void apply_shouldAddAuthHeaderOnly_whenTransactionIdNotPresent() {
        when(serviceTokenService.generateToken()).thenReturn("mocked-service-jwt");

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertEquals("Bearer mocked-service-jwt", template.headers().get("Authorization").iterator().next());

        assertFalse(template.headers().containsKey(TransactionLogFilter.TRANSACTION_ID_HEADER));
    }
}