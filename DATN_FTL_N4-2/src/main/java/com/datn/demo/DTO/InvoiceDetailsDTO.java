// src/main/java/com/datn/demo/DTO/InvoiceDetailsDTO.java
package com.datn.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetailsDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String productName;
    private int quantity;
    private BigDecimal productPrice;
    private String movieName;
    private String cinemaName;
    private String roomName;
    private String seatName;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Getters and Setters
}