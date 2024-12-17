package com.datn.demo.Beans;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductBean {
	 private Integer productId;

	    @NotBlank(message = "Tên sản phẩm không được để trống")
	    @Size(min = 2, max = 100, message = "Tên sản phẩm phải từ 2 đến 100 ký tự")
	    private String productName;

	    @NotBlank(message = "Mô tả sản phẩm không được để trống")
	    private String productDescription;

	    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
	    private double productPrice;

	    @NotBlank(message = "URL hình ảnh không được để trống")
	    @Pattern(regexp = "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", message = "URL hình ảnh không hợp lệ")
	    private String productImage;
}
