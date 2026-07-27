package org.gymcrm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TransactionLogFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_KEY = "transactionId";
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String transactionId = UUID.randomUUID().toString();
        try {
            MDC.put(TRANSACTION_ID_KEY, transactionId);
            response.setHeader(TRANSACTION_ID_HEADER, transactionId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }
}