package com.myhotel.hotelmanagementms.controller;

import com.myhotel.hotelmanagementms.entity.Room;
import com.myhotel.hotelmanagementms.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotelmanagementms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/rooms")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/rooms/{roomType}")
    public List<Room> getRoomByRoomType(String roomType) {
        return roomService.getRoomByRoomType(roomType);
    }

    @PostMapping("/room")
    public Room addRoom(@RequestBody Room room) {
        return roomService.addRoom(room);
    }
}
