package com.datn.demo.Services;

import com.datn.demo.Entities.RoomEntity;
import com.datn.demo.Entities.SeatEntity;
import com.datn.demo.Repositories.RoomRepository;
import com.datn.demo.Repositories.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddRoomAndSeatService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    public RoomEntity addRoomWithSeats(String roomName, int numberOfSeats) {
        // Tạo phòng mới
        RoomEntity room = new RoomEntity();
        room.setRoomName(roomName);
        room = roomRepository.save(room);  // Lưu phòng vào DB

        // Tạo ghế cho phòng
        int seatCount = 0;   // Số ghế đã được tạo
        char row = 'A';      // Hàng ghế bắt đầu từ 'A'
        int maxSeatsPerRow = 10;  // Số ghế tối đa mỗi hàng (ví dụ: 10 ghế mỗi hàng)

        for (int i = 1; seatCount < numberOfSeats; i++) {
            // Tạo tên ghế, ví dụ: A1, A2, A3,...
            String seatName = row + Integer.toString(i);

            // Nếu đã hết số ghế trong hàng, chuyển sang hàng mới
            if (i > maxSeatsPerRow) {
                i = 1; // Reset số ghế trong hàng
                row++; // Chuyển sang hàng tiếp theo
            }

            if (row > 'Z') break; // Nếu vượt quá 'Z' thì dừng lại

            // Tạo ghế và xác định trạng thái và giá trị
            SeatEntity seat = new SeatEntity();
            seat.setSeatName(seatName);
            seat.setRoom(room); // Liên kết ghế với phòng

            // Phân loại ghế theo số lượng
            if (seatCount < 20) {
                seat.setStatus("thường"); // Ghế đầu tiên là "thường"
                seat.setSeatPrice(75000.0); // Giá ghế "thường" là 75000
            } else {
                seat.setStatus("VIP"); // Ghế còn lại là "VIP"
                seat.setSeatPrice(85000.0); // Giá ghế "VIP" là 85000
            }

            seatRepository.save(seat); // Lưu ghế vào DB
            seatCount++; // Tăng số ghế đã tạo
        }

        return room;  // Trả về phòng đã được tạo và ghế đã được thêm
    }

}
