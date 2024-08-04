package com.myhotel.hotelmanagementms.controller;

import com.myhotel.hotelmanagementms.dto.ReserveRoomRequest;
import com.myhotel.hotelmanagementms.dto.ReserveRoomResponse;
import com.myhotel.hotelmanagementms.service.HotelManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotelmanagementms")
public class ReserveRoomController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HotelManagementService hotelManagementService;

    @PostMapping("/reserveroom")
    public ReserveRoomResponse reserveRoom(@RequestBody ReserveRoomRequest reserveRoomRequest) {
        logger.info("ReserveRoomRequest: " + reserveRoomRequest);
        return hotelManagementService.reserveRoom(reserveRoomRequest);
    }

    @DeleteMapping("/cancelreservation/{reservationId}")
    public ReserveRoomResponse cancelReservation(@PathVariable Long reservationId) {
        logger.info("Cancel reservation with reservationId: " + reservationId);
        return hotelManagementService.cancelReservation(reservationId);
    }

}
