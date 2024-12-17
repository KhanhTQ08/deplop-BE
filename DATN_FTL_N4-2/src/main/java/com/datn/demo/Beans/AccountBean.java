package com.datn.demo.Beans;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AccountBean {

    private int accountId; // Mã tài khoản

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 255, message = "Tên đăng nhập không được dài hơn 255 ký tự")
    private String username; // Tên đăng nhập

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải có độ dài từ 6 đến 255 ký tự")
    private String password; // Mật khẩu

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 150, message = "Họ và tên không được dài hơn 150 ký tự")
    private String fullName; // Họ và tên

    @Pattern(regexp = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber; // Số điện thoại

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được dài hơn 255 ký tự")
    private String email; // Email

    private Integer roleId; // Mã vai trò
}
