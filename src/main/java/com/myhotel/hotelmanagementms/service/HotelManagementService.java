package com.myhotel.hotelmanagementms.service;

import com.myhotel.hotelmanagementms.client.CustomerServiceClient;
import com.myhotel.hotelmanagementms.client.PaymentServiceClient;
import com.myhotel.hotelmanagementms.client.ReservationServiceClient;
import com.myhotel.hotelmanagementms.dto.*;
import com.myhotel.hotelmanagementms.entity.Room;
import com.myhotel.hotelmanagementms.util.Constant;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class HotelManagementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RoomService roomService;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private ReservationServiceClient reservationServiceClient;

    public ReserveRoomResponse reserveRoom(ReserveRoomRequest reserveRoomRequest) {
        ReserveRoomResponse reserveRoomResponse = getReserveRoomResponse(reserveRoomRequest);
        Customer customer = getCustomer(reserveRoomRequest);

        //Save customer data
        //Not an important service, reservation will be done if failure as well
        //Just logging the error in case of failure
        customerServiceClient.createCustomer(customer).subscribe(createdCustomer -> {
            logger.info("Customer created: " + createdCustomer);
        }, error -> {
            logger.error("Error while saving customer: " + error);
        });

        //Check available room
        List<Room> availableRooms = roomService.getRoomByRoomTypeAndStatus(
                reserveRoomRequest.getRoomType(),
                Constant.RoomStatus.AVAILABLE.name());
        logger.info("Available rooms: " + availableRooms);
        if (availableRooms.isEmpty()) {
            reserveRoomResponse.setReservationStatus(Constant.ReservationStatus.NOT_AVAILABLE.name());
            return reserveRoomResponse;
        }

        //Pick a room
        Room room = availableRooms.get(0);
        logger.info("Room picked: " + room);

        //Complete payment
        Payment payment = getPayment(reserveRoomRequest, room);
        String paymentId = paymentServiceClient.completePayment(payment);
        logger.info("Payment completed with transactionId: " + paymentId);
        if (paymentId == null) {
            logger.info("Error while doing payment: " + payment);
            reserveRoomResponse.setReservationStatus(Constant.PaymentStatus.PAYMENT_FAILED.name());
            return reserveRoomResponse;
        }

        //Reserve room
        Reservation reservation = getReservation(reserveRoomRequest, room, paymentId);
        Reservation savedReservation = reservationServiceClient.completeReservation(reservation);
        if (savedReservation == null) {
            Payment savedPayment = paymentServiceClient.getPaymentById(paymentId);
            logger.info("Payment saved: " + savedPayment);
            //Refund payment
            savedPayment.setPaymentStatus(Constant.PaymentStatus.CANCELLED.name());
            String cancelledPaymentId = paymentServiceClient.cancelPayment(savedPayment);
            if (cancelledPaymentId == null) {
                logger.info("Error while cancelling payment: " + savedPayment);
                reserveRoomResponse.setReservationStatus(
                        Constant.ReservationStatus.RESERVATION_FAILED_PAYMENT_NOT_REFUNDED.name());
            } else {
                logger.info("Payment cancelled with paymentId: " + cancelledPaymentId);
                reserveRoomResponse.setReservationStatus(
                        Constant.ReservationStatus.RESERVATION_FAILED.name());
            }
            return reserveRoomResponse;
        }

        room.setStatus(Constant.RoomStatus.RESERVED.name());
        room.setReservationId(savedReservation.getId());
        room.setPaymentId(paymentId);
        room.setEmail(reserveRoomRequest.getEmail());
        logger.info("Room booked: " + room);
        roomService.updateRoom(room);

        reserveRoomResponse.setReservationId(savedReservation.getId());
        reserveRoomResponse.setRoomNumber(room.getRoomNumber());
        reserveRoomResponse.setPrice(room.getPrice());
        reserveRoomResponse.setPaymentStatus(Constant.PaymentStatus.COMPLETED.name());
        reserveRoomResponse.setPaymentId(paymentId);
        reserveRoomResponse.setReservationStatus(savedReservation.getReservationStatus());
        logger.info("reserveRoomResponse: " + reserveRoomResponse);

        return reserveRoomResponse;
    }

    public ReserveRoomResponse cancelReservation(Long reservationId) {

        ReserveRoomResponse reserveRoomResponse = new ReserveRoomResponse();

        Reservation reservation = reservationServiceClient.getReservation(reservationId);
        if (reservation != null) {
            reservation.setReservationStatus(Constant.ReservationStatus.CANCELLED.name());
            Reservation updatedReservation = reservationServiceClient.updateReservation(reservation);
            if (updatedReservation != null) {
                logger.info("Reservation cancelled with reservationId: " + reservation.getId());
                reserveRoomResponse.setReservationStatus(Constant.ReservationStatus.CANCELLED.name());
            }
        } else {
            reserveRoomResponse.setReservationStatus(Constant.ReservationStatus.RESERVATION_NOT_FOUND.name());
            logger.info("No reservation found for reservationId: " + reservationId);
            return reserveRoomResponse;
        }

        Customer customer = customerServiceClient.getCustomerByEmail(reservation.getEmail());
        logger.info("Customer found: " + customer);
        if (customer != null) {
            reserveRoomResponse.setEmail(customer.getEmail());
            reserveRoomResponse.setFirstName(customer.getFirstName());
            reserveRoomResponse.setLastName(customer.getLastName());
            reserveRoomResponse.setPhone(customer.getPhone());
            reserveRoomResponse.setAddress(customer.getAddress());
        }

        Payment payment = paymentServiceClient.getPaymentById(reservation.getPaymentId());
        if (payment != null) {
            payment.setPaymentStatus(Constant.PaymentStatus.REFUNDED.name());
            String refundedPaymentId = paymentServiceClient.cancelPayment(payment);
            if (refundedPaymentId != null) {
                logger.info("Payment refunded with transactionId: " + payment.getPaymentTransactionId());
                reserveRoomResponse.setPaymentId(payment.getPaymentId());
                reserveRoomResponse.setPaymentStatus(Constant.PaymentStatus.REFUNDED.name());
            } else {
                logger.info("Error while refunding payment: " + payment);
                reserveRoomResponse.setPaymentStatus(Constant.PaymentStatus.NOT_REFUNDED.name());
            }
        } else {
            reserveRoomResponse.setPaymentStatus(Constant.PaymentStatus.INVALID_PAYMENT.name());
            logger.info("No payment found for email: " + reservation.getEmail());
            return reserveRoomResponse;
        }
        reserveRoomResponse.setRoomType(reservation.getRoomType());
        reserveRoomResponse.setRoomNumber(reservation.getRoomNumber());
        reserveRoomResponse.setPrice(reservation.getPrice());
        reserveRoomResponse.setReservationId(reservationId);
        logger.info("reserveRoomResponse: " + reserveRoomResponse);

        Room room = roomService.getRoomByRoomNumber(reservation.getRoomNumber());
        room.setStatus(Constant.RoomStatus.AVAILABLE.name());
        room.setReservationId(null);
        room.setPaymentId(null);
        room.setEmail(null);
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

    private Reservation getReservation(ReserveRoomRequest reserveRoomRequest, Room room, String paymentId) {
        Reservation reservation = new Reservation();
        reservation.setEmail(reserveRoomRequest.getEmail());
        reservation.setRoomNumber(room.getRoomNumber());
        reservation.setRoomType(reserveRoomRequest.getRoomType());
        reservation.setPaymentId(paymentId);
        reservation.setPrice(room.getPrice());
        return reservation;
    }

}
