package com.myhotel.hotelmanagementms.client;

import com.myhotel.hotelmanagementms.config.ServiceInstanceRetriever;
import com.myhotel.hotelmanagementms.dto.Payment;
import com.myhotel.hotelmanagementms.util.Constant;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PaymentServiceClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${system.service.paymentServiceName}")
    private String paymentServiceName;

    @Autowired
    private ServiceInstanceRetriever serviceInstanceRetriever;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @CircuitBreaker(name="completePayment", fallbackMethod = "fallbackCompletePayment")
    public String completePayment(Payment payment) {
        logger.info("Calling payment service to complete: " + payment);
        String paymentServiceUrl = serviceInstanceRetriever.getServiceUrl(paymentServiceName);
        logger.info("paymentServiceUrl : " + paymentServiceUrl);
        if(paymentServiceUrl == null)
            return null;
        return webClientBuilder.baseUrl(paymentServiceUrl + Constant.PAYMENT_SERVICE_PATH)
                .build()
                .post()
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Retry(name="cancelPayment", fallbackMethod = "fallbackCancelPayment")
    public String cancelPayment(Payment payment) {
        logger.info("Calling payment service to refund: " + payment);
        String paymentServiceUrl = serviceInstanceRetriever.getServiceUrl(paymentServiceName);
        logger.info("paymentServiceUrl : " + paymentServiceUrl);
        if(paymentServiceUrl == null)
            return null;
        return webClientBuilder.baseUrl(paymentServiceUrl + Constant.PAYMENT_SERVICE_PATH)
                .build()
                .put()
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Retry(name="getPayment", fallbackMethod = "fallbackGetPayment")
    public Payment getPaymentById(String paymentId) {
        logger.info("Calling payment service to retrieve: " + paymentId);
        String paymentServiceUrl = serviceInstanceRetriever.getServiceUrl(paymentServiceName);
        logger.info("paymentServiceUrl : " + paymentServiceUrl);
        if(paymentServiceUrl == null)
            return null;
        return webClientBuilder.baseUrl(paymentServiceUrl + Constant.PAYMENT_SERVICE_PATH)
                .build()
                .get()
                .uri("/" + paymentId)
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
    }

    public String fallbackCompletePayment(Exception e) {
        logger.info("Entering fallback while doing payment: "
                + e.getMessage());
        return null;
    }

    public String fallbackCancelPayment(Exception e) {
        logger.info("Entering fallback while cancelling or refunding payment: "
                + e.getMessage());
        return null;
    }

    public Payment fallbackGetPayment(Exception e) {
        logger.info("Entering fallback while retrieving payment: "
                + e.getMessage());
        return null;
    }
}
