package com.datn.demo.Beans;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MovieBean {
	 private Integer movieId; // Mã phim

	    @NotBlank(message = "Tên phim không được để trống")
	    @Size(max = 255, message = "Tên phim không được vượt quá 255 ký tự")
	    private String movieName; // Tên phim

	    @NotBlank(message = "URL trailer không được để trống")
	    private String trailerUrl; // Đường dẫn trailer

	    @NotBlank(message = "Nội dung phim không được để trống")
	    private String content; // Nội dung phim

	    @NotBlank(message = "Giới hạn độ tuổi không được để trống")
	    @Pattern(regexp = "^\\d{1,2}$", message = "Giới hạn độ tuổi phải là số trong khoảng 0 đến 99")
	    private String ageRestriction; // Giới hạn độ tuổi

	    @NotNull(message = "Thời gian phim không được để trống")
	    @Min(value = 1, message = "Thời gian phim phải lớn hơn 0 phút")
	    @Max(value = 600, message = "Thời gian phim không được vượt quá 600 phút")
	    private Integer duration; // Thời gian phim

	    @NotBlank(message = "Đạo diễn không được để trống")
	    @Size(max = 255, message = "Tên đạo diễn không được vượt quá 255 ký tự")
	    private String director; // Đạo diễn

	    @NotBlank(message = "Diễn viên không được để trống")
	    @Size(max = 255, message = "Tên diễn viên không được vượt quá 255 ký tự")
	    private String actor; // Diễn viên

	    @NotBlank(message = "URL hình ảnh không được để trống")
	    @Pattern(regexp = "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", message = "URL hình ảnh không hợp lệ")
	    private String image; // Hình ảnh

	    @NotBlank(message = "Thể loại phim không được để trống")
	    private String genre; // Thể loại phim

	    @NotBlank(message = "Hình nền phim không được để trống")
	    @Pattern(regexp = "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", message = "URL hình nền không hợp lệ")
	    private String image_bg; // Hình nền phim
}
