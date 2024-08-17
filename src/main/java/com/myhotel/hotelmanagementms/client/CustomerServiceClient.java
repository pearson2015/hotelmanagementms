package com.myhotel.hotelmanagementms.client;

import com.myhotel.hotelmanagementms.dto.Customer;
import com.myhotel.hotelmanagementms.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class CustomerServiceClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${system.api.customerServiceUrl}")
    private String customerServiceUrl;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Mono<Customer> createCustomer(Customer customer) {
        logger.info("Calling createCustomer : " + customer);
        return webClientBuilder.baseUrl(customerServiceUrl + Constant.CUSTOMER_SERVICE_PATH)
                .build()
                .post()
                .bodyValue(customer)
                .retrieve()
                .bodyToMono(Customer.class);
    }

    @CircuitBreaker(name="getCustomer", fallbackMethod = "fallbackGetCustomer")
    public Customer getCustomerByEmail(String email) {
        logger.info("Calling customer service by email: " +email);
        return webClientBuilder.baseUrl(customerServiceUrl + Constant.CUSTOMER_SERVICE_PATH)
                .build()
                .get()
                .uri("/email/" + email)
                .retrieve()
                .bodyToMono(Customer.class)
                .block();
    }

    public Customer fallbackGetCustomer(Exception e) {
        logger.info("Entering fallback while calling getCustomerByEmail : "
                + e.getMessage());
        return null;
    }
}
