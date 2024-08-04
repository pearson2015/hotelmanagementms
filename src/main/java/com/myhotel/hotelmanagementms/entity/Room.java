package com.myhotel.hotelmanagementms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "room")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String roomNumber;
    private String roomType;
    private Double price;
    private String status;
    private String paymentTransactionId;
    private Long reservationId;
}
