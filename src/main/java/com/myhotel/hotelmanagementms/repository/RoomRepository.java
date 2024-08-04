package com.myhotel.hotelmanagementms.repository;

import com.myhotel.hotelmanagementms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>{

    public Room findByRoomNumber(String roomNumber);

    public List<Room> findByRoomType(String roomType);

    public List<Room> findByRoomTypeAndStatus(String roomType, String status);
}
