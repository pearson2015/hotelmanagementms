package com.myhotel.hotelmanagementms.client;

import com.myhotel.hotelmanagementms.dto.Payment;
import com.myhotel.hotelmanagementms.dto.Reservation;
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
public class ReservationServiceClient {

    @Autowired
    @Qualifier("reservationWebClient")
    private WebClient reservationWebClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @CircuitBreaker(name="completeReservation", fallbackMethod = "fallbackConnectReservation")
    public Reservation completeReservation(Reservation reservation) {
        logger.info("Calling reservation service: " +reservation);
        return reservationWebClient.post()
                .bodyValue(reservation)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
    }

    @CircuitBreaker(name="getReservation", fallbackMethod = "fallbackConnectReservation")
    public Reservation getReservation(Long reservationId) {
        logger.info("Calling reservation service for reservationId: " +reservationId);
        return reservationWebClient.get()
                .uri(Constant.GET_RESERVATION_SERVICE_PATH + reservationId)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
    }

    @CircuitBreaker(name="updateReservation", fallbackMethod = "fallbackConnectReservation")
    public Reservation updateReservation(Reservation reservation) {
        logger.info("Calling reservation service to update: " +reservation);
        return reservationWebClient.put()
                .bodyValue(reservation)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
    }

    public Reservation fallbackConnectReservation(Exception e) {
        logger.info("Entering fallback while calling reservation service: "
                + e.getMessage());
        return null;
    }
}
