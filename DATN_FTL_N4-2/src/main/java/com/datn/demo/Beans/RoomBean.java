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
public class RoomBean {
	private int roomId; // Mã phòng
	
    @NotBlank(message = "tên phòng không được để trống")
	private String roomName; // Tên phòng
}
