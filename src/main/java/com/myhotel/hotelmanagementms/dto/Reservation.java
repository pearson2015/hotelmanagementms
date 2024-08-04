package com.myhotel.hotelmanagementms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {
    private Long id;
    private String email;
    private String roomNumber;
    private String roomType;
    private Date reservationDate;
    private String reservationStatus;
    private double price;
    private String paymentTransactionId;
}
