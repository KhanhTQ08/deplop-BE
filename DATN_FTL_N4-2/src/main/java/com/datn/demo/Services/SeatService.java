package com.datn.demo.Services;

import com.datn.demo.Entities.RoomEntity;
import com.datn.demo.Entities.SeatEntity;
import com.datn.demo.Repositories.RoomRepository;
import com.datn.demo.Repositories.SeatRepository; // Nhập khẩu SeatRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeatService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository; // Khai báo SeatRepository

    public Optional<SeatEntity> findById(int seatId) {
        return seatRepository.findById(seatId); // Truy vấn ghế theo ID
    }

    public List<SeatEntity> getSeatsByRoomId(int roomId) {
        return seatRepository.findByRoomRoomId(roomId);
    }

    public SeatEntity findSeatById(Integer seatId) {
        // Tìm ghế theo ID trong cơ sở dữ liệu
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found with id: " + seatId));
    }

    public SeatEntity getSeatById(Integer seatId) {
        return seatRepository.findById(seatId).orElse(null); // Nếu không tìm thấy, trả về null
    }

    public int countSeatsInRoom(int roomId) {
        return seatRepository.countSeatsByRoomId(roomId);
    }

    public boolean updateSeatPrice(Integer seatId, Double seatPrice) {
        Optional<SeatEntity> seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isPresent()) {
            SeatEntity seat = seatOpt.get();
            seat.setSeatPrice(seatPrice);  // Cập nhật giá ghế
            seatRepository.save(seat);     // Lưu lại vào cơ sở dữ liệu
            return true;
        }
        return false; // Không tìm thấy ghế để cập nhật
    }


    public boolean addSeat(Integer roomId, String seatName, String status, Double seatPrice) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại."));

        // Kiểm tra số lượng ghế trong phòng
        if (room.getSeats().size() >= 49) {
            return false; // Phòng đã đủ ghế
        }

        // Tạo ghế mới
        SeatEntity newSeat = new SeatEntity();
        newSeat.setRoom(room);
        newSeat.setSeatName(seatName);
        newSeat.setStatus(status);
        newSeat.setSeatPrice(seatPrice);

        // Lưu ghế mới
        seatRepository.save(newSeat);
        return true;
    }

    public boolean deleteSeat(Integer seatId) {
        if (seatRepository.existsById(seatId)) {
            seatRepository.deleteById(seatId);  // Xóa ghế nếu tồn tại
            return true;
        }
        return false;
    }

    public boolean isSeatNameExist(Integer roomId, String seatName) {
        // Kiểm tra trong cơ sở dữ liệu xem tên ghế đã tồn tại trong phòng chưa
        List<SeatEntity> seats = seatRepository.findByRoomRoomId(roomId);
        for (SeatEntity seat : seats) {
            if (seat.getSeatName().equalsIgnoreCase(seatName)) {
                return true; // Nếu tìm thấy tên ghế trùng
            }
        }
        return false; // Không có tên ghế trùng
    }

    public List<SeatEntity> getSeatsWithRooms() {

        return seatRepository.findSeatsWithRooms();
    }

    // Thêm phương thức saveSeats
    public void saveSeats(List<SeatEntity> seats) {
        seatRepository.saveAll(seats); // Sử dụng phương thức saveAll của JpaRepository
    }

    // Tìm danh sách ghế theo roomId
    public List<SeatEntity> findByRoomRoomId(Integer roomId) {
        return seatRepository.findByRoomRoomId(roomId);
    }

    public SeatEntity save(SeatEntity seat) {
        return seatRepository.save(seat);
    }


}
