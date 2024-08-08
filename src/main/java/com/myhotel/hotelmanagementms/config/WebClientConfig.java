package com.myhotel.hotelmanagementms.config;

import com.myhotel.hotelmanagementms.util.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${system.api.customerServiceUrl}")
    private String customerServiceUrl;

    @Value("${system.api.paymentServiceUrl}")
    private String paymentServiceUrl;

    @Value("${system.api.reservationServiceUrl}")
    private String reservationServiceUrl;

    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean(name = "customerWebClient")
    public WebClient customerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(customerServiceUrl + Constant.CUSTOMER_SERVICE_PATH)
                .build();
    }

    @Bean(name = "paymentWebClient")
    public WebClient paymentWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(paymentServiceUrl + Constant.PAYMENT_SERVICE_PATH)
                .build();
    }

    @Bean(name = "reservationWebClient")
    public WebClient reservationWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(reservationServiceUrl + Constant.RESERVATION_SERVICE_PATH)
                .build();
    }

}
