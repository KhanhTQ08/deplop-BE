package com.datn.demo.DTO;

import lombok.Data;

@Data
public class SeatUpdateDTO {
    private String seatId;   // ID của ghế
    private String status;   // Trạng thái ghế (selected, available)
    private String updatedBy; // Client ID đã chọn ghế
}
