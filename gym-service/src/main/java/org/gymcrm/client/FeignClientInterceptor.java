package org.gymcrm.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.gymcrm.filter.TransactionLogFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String transactionId = MDC.get(TransactionLogFilter.TRANSACTION_ID_KEY);
        if (transactionId != null) {
            template.header(TransactionLogFilter.TRANSACTION_ID_HEADER, transactionId);
        }
    }
}
