package com.myhotel.hotelmanagementms.service;

import com.myhotel.hotelmanagementms.dto.Payment;
import com.myhotel.hotelmanagementms.util.Constant;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PaymentServiceClient {

    @Autowired
    @Qualifier("paymentWebClient")
    private WebClient paymentWebClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @CircuitBreaker(name="completePayment", fallbackMethod = "fallbackCompletePayment")
    public Payment completePayment(Payment payment) {
        return paymentWebClient.post()
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
    }

    @Retry(name="cancelPayment", fallbackMethod = "fallbackCancelPayment")
    public Payment cancelPayment(Payment payment) {
        return paymentWebClient.put()
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
    }

    public Payment getPayment(String paymentTransactionId) {
        return paymentWebClient.get()
                .uri(Constant.GET_PAYMENT_SERVICE_PATH + paymentTransactionId)
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
    }

    public Payment fallbackCompletePayment(Exception e) {
        logger.info("Entering fallback while doing payment: "
                + e.getMessage());
        return null;
    }

    public Payment fallbackCancelPayment(Exception e) {
        logger.info("Entering fallback while cancelling or refunding payment: "
                + e.getMessage());
        return null;
    }
}
