package com.datn.demo.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CINEMA_INFORMATION")
public class CinemaInformationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CINEMA_ID")
    private Integer cinemaId;

    @NotBlank(message = "Tên rạp không được để trống")
    @Size(min = 2, max = 100, message = "Tên rạp phải có độ dài từ 2 đến 100 ký tự")
    @Column(name = "CINEMA_NAME")
    private String cinemaName;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 2, max = 255, message = "địa chỉ phải có độ dài từ 2 đến 255 ký tự")
    @Column(name = "ADDRESS")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại phải bắt đầu từ 0, chỉ chứa chữ số và có độ dài từ 10 đến 11 ký tự")
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "EMAIL")
    private String email;
    @OneToMany(mappedBy = "cinemaInformation", fetch = FetchType.LAZY)
    private List<ShowtimeEntity> showtimes;

    @Transient // Không lưu trữ vào cơ sở dữ liệu
    private List<MovieEntity> movies; // Danh sách phim đã được nhóm theo suất chiếu
}
