package com.datn.demo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SeatSelectionMessage {
    private String seatId; // ID của ghế
    private boolean selected; // true nếu ghế được chọn, false nếu bỏ chọn
    private String username; // (Tùy chọn) Tên người dùng chọn ghế

    // Getters và Setters

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}