package com.datn.demo.Beans;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROMOTION_ID")
    private int promotionId;

    @Column(name = "PROMOTION_NAME", nullable = false)
    private String promotionName;

    @Column(name = "DISCOUNT_VALUE", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "PROMOTION_DESCRIPTION", columnDefinition = "nvarchar(max)")
    private String promotionDescription;
}
