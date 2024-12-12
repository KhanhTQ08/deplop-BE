package com.datn.demo.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieStatisticDTO {
    private String movieName; // Tên phim
    private Long ticketCount; // Số vé bán
    private BigDecimal totalRevenue; // Tổng doanh thu
}
