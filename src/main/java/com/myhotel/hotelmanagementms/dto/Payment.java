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
public class Payment {
    private String paymentId;
    private String email;
    private double price;
    private String paymentMethod;
    private String paymentStatus;
    private Date paymentDate;
    private String paymentTransactionId;

}
