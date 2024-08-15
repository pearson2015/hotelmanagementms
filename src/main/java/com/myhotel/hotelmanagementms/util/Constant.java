package com.myhotel.hotelmanagementms.util;

public class Constant {
    public static final String CUSTOMER_SERVICE_PATH = "/customerms/customer";
    public static final String PAYMENT_SERVICE_PATH = "/paymentms/payment";

    public static final String GET_PAYMENT_SERVICE_PATH = "/transactionid/";
    public static final String RESERVATION_SERVICE_PATH = "/reservationms/reservation";

    public static final String GET_RESERVATION_SERVICE_PATH = "/id/";

    public enum ReservationStatus {
        RESERVED,
        CANCELLED,
        NOT_AVAILABLE,

        RESERVATION_FAILED,
        RESERVATION_FAILED_PAYMENT_NOT_REFUNDED,
        RESERVATION_NOT_FOUND
    }

    public enum RoomStatus {
        AVAILABLE,
        RESERVED,
        OCCUPIED
    }

    public enum PaymentStatus {
        COMPLETED,
        CANCELLED,
        REFUNDED,
        NOT_REFUNDED,
        PAYMENT_FAILED,
        INVALID_PAYMENT
    }
}
