package com.datn.demo.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetailDTO {
	 private Integer invoiceId;
	    private Integer accountId;
	    private Integer showtimeId;
	    private LocalDate bookingDate;
	    private LocalTime bookingTime;
	    private BigDecimal totalAmount;
	    private String status;

    
    private Integer detailId;
    private Integer productId;
    private Integer quantity;
    private Double price;
    private Double totalPrice;

    public Integer getProductId() {
        return productId;
    }
}
