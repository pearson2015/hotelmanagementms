package com.myhotel.hotelmanagementms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelmanagementmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelmanagementmsApplication.class, args);
	}

}
