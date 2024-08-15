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
public class ReserveRoomResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String roomType;
    private String roomNumber;
    private Long reservationId;
    private String reservationStatus;
    private Double price;
    private String paymentId;
    private String paymentStatus;

}
