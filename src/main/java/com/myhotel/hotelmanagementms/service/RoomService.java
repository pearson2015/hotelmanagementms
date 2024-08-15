package com.myhotel.hotelmanagementms.service;

import com.myhotel.hotelmanagementms.entity.Room;
import com.myhotel.hotelmanagementms.repository.RoomRepository;
import com.myhotel.hotelmanagementms.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> getRoomByRoomType(String roomType) {
        return roomRepository.findByRoomType(roomType);
    }

    public List<Room> getRoomByRoomTypeAndStatus(String roomType, String status) {
        return roomRepository.findByRoomTypeAndStatus(roomType, status);
    }

    public Room addRoom(Room room) {
        room.setStatus(Constant.RoomStatus.AVAILABLE.name());
        return roomRepository.save(room);
    }

    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public Room getRoomByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

}
