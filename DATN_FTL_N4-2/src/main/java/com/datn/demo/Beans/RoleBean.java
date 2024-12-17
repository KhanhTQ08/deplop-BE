package com.datn.demo.Beans;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleBean {

    private int roleId;
    
    @NotBlank(message = "tên phòng không được để trống")
    private String roleName;
}
