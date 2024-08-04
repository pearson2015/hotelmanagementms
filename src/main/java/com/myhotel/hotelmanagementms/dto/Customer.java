package com.myhotel.hotelmanagementms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}
