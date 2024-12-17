package com.datn.demo.Services;

import com.datn.demo.DTO.SeatUpdateDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeatService2 {

    // Sử dụng ConcurrentHashMap để lưu trạng thái ghế
    private final Map<String, SeatUpdateDTO> seatStatusMap = new ConcurrentHashMap<>();

    // Lấy tất cả trạng thái ghế
    public Map<String, SeatUpdateDTO> getAllSeatStatus() {
        return seatStatusMap;
    }

    // Cập nhật trạng thái ghế
    public void updateSeatStatus(String seatId, String status, String updatedBy) {
        SeatUpdateDTO seatUpdate = new SeatUpdateDTO();
        seatUpdate.setSeatId(seatId);
        seatUpdate.setStatus(status);
        seatUpdate.setUpdatedBy(updatedBy);
        seatStatusMap.put(seatId, seatUpdate);
    }

    // Reset trạng thái ghế
    public void resetSeatStatus(String seatId) {
        seatStatusMap.remove(seatId);
    }
}
