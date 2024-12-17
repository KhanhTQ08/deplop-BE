package com.datn.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ROOM")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROOM_ID")
    private int roomId; // Mã phòng

    @Column(name = "ROOM_NAME", nullable = false)
    private String roomName; // Tên phòng

    // Thêm thuộc tính danh sách ghế
    @OneToMany(mappedBy = "room") // mappedBy là tên thuộc tính trong SeatEntity
    private List<SeatEntity> seats; // Danh sách ghế

    @ManyToOne
    @JoinColumn(name = "CINEMA_ID", referencedColumnName = "CINEMA_ID", nullable = false)
    private CinemaInformationEntity cinema; // FK tới Cinema

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<ShowtimeEntity> showtimes; // Danh sách suất chiếu

    @Column(name = "IS_DELECTED", nullable = true)
    private boolean isDeleted = true; // Cờ đánh dấu trạng thái xóa mềm
    
    // Getter cho danh sách ghế
    public List<SeatEntity> getSeats() {
        return seats;
    }
}
