package com.myhotel.hotelmanagementms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myhotel.hotelmanagementms.dto.*;
import com.myhotel.hotelmanagementms.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class HotelManagementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectMapper  objectMapper = new ObjectMapper();

    @Value("${system.api.customerServiceUrl}")
    private String customerServiceUrl;

    @Value("${system.api.paymentServiceUrl}")
    private String paymentServiceUrl;

    @Value("${system.api.reservationServiceUrl}")
    private String reservationServiceUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private RoomService roomService;

    public ReserveRoomResponse reserveRoom(ReserveRoomRequest reserveRoomRequest) {
        ReserveRoomResponse reserveRoomResponse = getReserveRoomResponse(reserveRoomRequest);
        Customer customer = getCustomer(reserveRoomRequest);

        //Save customer data
        Customer savedCustomer = webClient.post()
                .uri(customerServiceUrl + "/customerms/customer")
                .bodyValue(customer)
                .retrieve()
                .bodyToMono(Customer.class)
                .block();
        if(savedCustomer == null) {
            logger.info("Error while saving customer: " + customer);
            reserveRoomResponse.setReservationStatus("ERROR");
            return reserveRoomResponse;
        }
        logger.info("Customer saved: " + savedCustomer);

        //Check available room
        List<Room> availableRooms = roomService.getRoomByRoomTypeAndStatus(reserveRoomRequest.getRoomType(), "AVAILABLE");
        logger.info("Available rooms: " + availableRooms);
        if(availableRooms.isEmpty()) {
            reserveRoomResponse.setReservationStatus("NOT_AVAILABLE");
            return reserveRoomResponse;
        }

        //Pick a room
        Room room = availableRooms.getFirst();
        logger.info("Room picked: " + room);

        //Complete payment
        Payment payment = getPayment(reserveRoomRequest, room);
        Payment savedPayment = webClient.post()
                .uri(paymentServiceUrl + "/paymentms/payment")
                .bodyValue(payment)
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
        logger.info("Payment saved: " + savedPayment);
        if(savedPayment == null) {
            logger.info("Error while doing payment: " + payment);
            reserveRoomResponse.setReservationStatus("PAYMENT_FAILED");
            return reserveRoomResponse;
        }

        //Reserve room
        Reservation reservation = getReservation(reserveRoomRequest, room, savedPayment);
        Reservation savedReservation = webClient.post()
                .uri(reservationServiceUrl + "/reservationms/reservation")
                .bodyValue(reservation)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
        if(savedReservation == null) {
            reserveRoomResponse.setReservationStatus("RESERVATION_FAILED");
            //Refund payment
            boolean isRefunded = Boolean.TRUE.equals(webClient.delete()
                    .uri(paymentServiceUrl + "/paymentms/payment/" + savedPayment.getId())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
            logger.info("Payment refunded with transactionId: " + savedPayment.getPaymentTransactionId());
            return reserveRoomResponse;
        }

        room.setStatus("BOOKED");
        room.setReservationId(savedReservation.getId());
        room.setPaymentTransactionId(savedReservation.getPaymentTransactionId());
        logger.info("Room booked: " + room);
        roomService.updateRoom(room);

        reserveRoomResponse.setReservationId(savedReservation.getId());
        reserveRoomResponse.setRoomNumber(room.getRoomNumber());
        reserveRoomResponse.setPrice(room.getPrice());
        reserveRoomResponse.setPaymentStatus("PAID");
        reserveRoomResponse.setPaymentTransactionId(savedPayment.getPaymentTransactionId());
        reserveRoomResponse.setReservationStatus("SUCCESS");
        logger.info("reserveRoomResponse: " + reserveRoomResponse);

        return reserveRoomResponse;
    }

    public ReserveRoomResponse cancelReservation(Long reservationId) {

        ReserveRoomResponse reserveRoomResponse = new ReserveRoomResponse();

        Reservation reservation = webClient.get()
                .uri(reservationServiceUrl + "/reservationms/reservation/id/" + reservationId)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
        if(reservation != null) {
            reservation.setReservationStatus("CANCELLED");
            Reservation updatedReservation = webClient.put()
                    .uri(reservationServiceUrl + "/reservationms/reservation")
                    .bodyValue(reservation)
                    .retrieve()
                    .bodyToMono(Reservation.class)
                    .block();
            if (updatedReservation != null) {
                logger.info("Reservation cancelled with reservationId: " + reservation.getId());
                reserveRoomResponse.setReservationStatus("CANCELLED");
            }
        } else {
            reserveRoomResponse.setReservationStatus("RESERVATION_NOT_FOUND");
            logger.info("No reservation found for reservationId: " + reservationId);
            return reserveRoomResponse;
        }

        Customer customer = webClient.get()
                .uri(customerServiceUrl + "/customerms/customer/email/" + reservation.getEmail())
                .retrieve()
                .bodyToMono(Customer.class)
                .block();
        logger.info("Customer found: " + customer);
        if(customer != null) {
            reserveRoomResponse.setEmail(customer.getEmail());
            reserveRoomResponse.setFirstName(customer.getFirstName());
            reserveRoomResponse.setLastName(customer.getLastName());
            reserveRoomResponse.setPhone(customer.getPhone());
            reserveRoomResponse.setAddress(customer.getAddress());
        }

        Payment payment = webClient.get()
                .uri(paymentServiceUrl + "/paymentms/payment/transactionid/" + reservation.getPaymentTransactionId())
                .retrieve()
                .bodyToMono(Payment.class)
                .block();
        if(payment != null) {
            boolean isRefunded = Boolean.TRUE.equals(webClient.delete()
                    .uri(paymentServiceUrl + "/paymentms/payment/" + payment.getId())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
            if (isRefunded) {
                logger.info("Payment refunded with transactionId: " + payment.getPaymentTransactionId());
                reserveRoomResponse.setPaymentTransactionId(payment.getPaymentTransactionId());
                reserveRoomResponse.setPaymentStatus("REFUNDED");
            }
        } else {
            reserveRoomResponse.setPaymentStatus("NOT_REFUNDED");
            logger.info("No payment found for email: " + reservation.getEmail());
            return reserveRoomResponse;
        }
        reserveRoomResponse.setRoomType(reservation.getRoomType());
        reserveRoomResponse.setRoomNumber(reservation.getRoomNumber());
        reserveRoomResponse.setPrice(reservation.getPrice());
        reserveRoomResponse.setReservationId(reservationId);
        logger.info("reserveRoomResponse: " + reserveRoomResponse);

        Room room = roomService.getRoomByRoomNumber(reservation.getRoomNumber());
        room.setStatus("AVAILABLE");
        room.setReservationId(null);
        room.setPaymentTransactionId(null);
        roomService.updateRoom(room);

        return reserveRoomResponse;
    }

    private ReserveRoomResponse getReserveRoomResponse(ReserveRoomRequest reserveRoomRequest) {
        ReserveRoomResponse reserveRoomResponse = new ReserveRoomResponse();
        reserveRoomResponse.setEmail(reserveRoomRequest.getEmail());
        reserveRoomResponse.setFirstName(reserveRoomRequest.getFirstName());
        reserveRoomResponse.setLastName(reserveRoomRequest.getLastName());
        reserveRoomResponse.setPhone(reserveRoomRequest.getPhone());
        reserveRoomResponse.setAddress(reserveRoomRequest.getAddress());
        reserveRoomResponse.setRoomType(reserveRoomRequest.getRoomType());
        return reserveRoomResponse;
    }

    private ReserveRoomResponse getReserveRoomResponse(Customer customer) {
        ReserveRoomResponse reserveRoomResponse = new ReserveRoomResponse();
        reserveRoomResponse.setEmail(customer.getEmail());
        reserveRoomResponse.setFirstName(customer.getFirstName());
        reserveRoomResponse.setLastName(customer.getLastName());
        reserveRoomResponse.setPhone(customer.getPhone());
        reserveRoomResponse.setAddress(customer.getAddress());
        return reserveRoomResponse;
    }

    private Customer getCustomer(ReserveRoomRequest reserveRoomRequest) {
        Customer customer = new Customer();
        customer.setEmail(reserveRoomRequest.getEmail());
        customer.setFirstName(reserveRoomRequest.getFirstName());
        customer.setLastName(reserveRoomRequest.getLastName());
        customer.setPhone(reserveRoomRequest.getPhone());
        customer.setAddress(reserveRoomRequest.getAddress());
        return customer;
    }

    private Payment getPayment(ReserveRoomRequest reserveRoomRequest, Room room) {
        Payment payment = new Payment();
        payment.setEmail(reserveRoomRequest.getEmail());
        payment.setPrice(room.getPrice());
        return payment;
    }

    private Reservation getReservation(ReserveRoomRequest reserveRoomRequest, Room room, Payment payment) {
        Reservation reservation = new Reservation();
        reservation.setEmail(reserveRoomRequest.getEmail());
        reservation.setRoomNumber(room.getRoomNumber());
        reservation.setRoomType(reserveRoomRequest.getRoomType());
        reservation.setPaymentTransactionId(payment.getPaymentTransactionId());
        reservation.setPrice(room.getPrice());
        return reservation;
    }

}
