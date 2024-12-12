package com.datn.demo.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PRODUCT")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private int productId;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "PRODUCT_DESCRIPTION")
    private String productDescription;

    @Column(name = "PRODUCT_PRICE", nullable = false)
    private double productPrice;

    @Column(name = "PRODUCT_IMAGE")
    private String productImage;

    @Column(name = "IS_DELECTED", nullable = true)
    private boolean isDeleted = true; // Cờ đánh dấu trạng thái xóa mềm
    
    public void setProductPrice(double productPrice) {
        if (productPrice < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm.");
        }
        this.productPrice = productPrice;
    }
}
