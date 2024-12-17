package com.datn.demo.Controllers;

import com.datn.demo.Entities.CinemaInformationEntity;
import com.datn.demo.Entities.RoomEntity;
import com.datn.demo.Entities.SeatEntity;
import com.datn.demo.Entities.CinemaInformationEntity; // Đảm bảo bạn có entity này
import com.datn.demo.Repositories.RoomRepository;
import com.datn.demo.Repositories.SeatRepository;
import com.datn.demo.Repositories.CinemaInformationRepository; // Đảm bảo bạn có repository này
import com.datn.demo.Services.AddRoomAndSeatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AddRoomAndSeatController {

    @Autowired
    private AddRoomAndSeatService addRoomAndSeatService; // Service class for business logic

    @Autowired
    private RoomRepository roomRepository; // Inject RoomRepository
    @Autowired
    private SeatRepository seatRepository; // Inject SeatRepository
    @Autowired
    private CinemaInformationRepository cinemaRepository; // Inject CinemaRepository

    @PostMapping("/add-room-seats")
    public ResponseEntity<?> addRoomAndSeats(@RequestParam String roomName,
                                             @RequestParam int soLuongGhe,
                                             @RequestParam String selectedSeats,
                                             @RequestParam int cinemaId) { // Lấy cinemaId từ request
        try {
            // Lấy cinema từ cinemaId
            CinemaInformationEntity cinema = cinemaRepository.findById(cinemaId)
                    .orElseThrow(() -> new Exception("Rạp chiếu không tồn tại"));

            // Parse selected seats (assuming it's a JSON string array)
            List<String> seats = new ObjectMapper().readValue(selectedSeats, new TypeReference<List<String>>() {});

            // Create and save the room entity
            RoomEntity room = new RoomEntity();
            room.setRoomName(roomName);
            room.setCinema(cinema); // Liên kết phòng với rạp chiếu
            room = roomRepository.save(room); // Save room to the database

            int seatCount = 0;  // Initialize seat counter

            // Create seat entities and save them
            for (String seatName : seats) {
                SeatEntity seat = new SeatEntity();
                seat.setSeatName(seatName);

                // Classify seats based on the count (first 20 seats are "thường", the rest are "VIP")
                if (seatCount < 20) {
                    seat.setStatus("Thường"); // First 20 seats are "thường"
                    seat.setSeatPrice(75000.0); // Price for "thường" is 75000
                } else {
                    seat.setStatus("VIP"); // The rest are "VIP"
                    seat.setSeatPrice(85000.0); // Price for "VIP" is 85000
                }

                seat.setRoom(room);  // Link seat to room
                seatRepository.save(seat); // Save seat to DB
                seatCount++; // Increment the seat counter
            }

            return ResponseEntity.ok("Phòng và ghế đã được thêm!");
        } catch (Exception e) {
            e.printStackTrace();  // Print error for debugging
            return ResponseEntity.status(500).body("Có lỗi khi thêm phòng và ghế. Lỗi: " + e.getMessage());
        }
    }


    //Push code 
}
