package com.myhotel.hotelmanagementms.service;

import com.myhotel.hotelmanagementms.entity.Room;
import com.myhotel.hotelmanagementms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.List;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RoomServiceTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @MockBean
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    private static Room room;

    @BeforeAll
    public static void setUp() {
        room = new Room(1L,
                "L52",
                "1BHK",
                100D,
                "AVAILABLE",
                null,
                null,
                null) {
        };
    }

    @Test
    @DisplayName("Test get all rooms")
    public void testGetAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        List<Room> rooms = roomService.getAllRooms();
        logger.info("Rooms: {}", rooms);
        assert !rooms.isEmpty();
    }

    @Test
    @DisplayName("Test get room by id")
    public void testGetRoomById() {
        when(roomRepository.findById(1L)).thenReturn(ofNullable(room));
        Room room = roomService.getRoomById(1L);
        logger.info("Room: {}", room);
        assert room != null;
    }

    @Test
    @DisplayName("Test get room by id not found")
    public void testGetRoomByIdNotFound() {
        when(roomRepository.findById(1L)).thenReturn(empty());
        Room room = roomService.getRoomById(1L);
        logger.info("Room: {}", room);
        assert room == null;
    }

}
