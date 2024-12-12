package com.datn.demo.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "SHOWTIME")
public class ShowtimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHOWTIME_ID")
    private int showtimeId;

    @ManyToOne
    @JoinColumn(name = "ROOM_ID", referencedColumnName = "ROOM_ID")
    private RoomEntity room;

    @ManyToOne
    @JoinColumn(name = "MOVIE_ID", referencedColumnName = "MOVIE_ID")
    private MovieEntity movie;

    @Column(name = "SHOW_DATE")
    private LocalDate showDate;

    @Column(name = "START_TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME")
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "CINEMA_ID", referencedColumnName = "CINEMA_ID")
    private CinemaInformationEntity cinemaInformation; // Liên kết với buổi chiếu

    @Column(name = "ORIGINAL_SHOW_DATE")
    private LocalDate originalShowDate;

    @Column(name = "ORIGINAL_START_TIME")
    private LocalTime originalStartTime;

    @Column(name = "ORIGINAL_END_TIME")
    private LocalTime originalEndTime;
    
    @Column(name = "REASON", length = 50)
    private String reason; // Thể loại phim
    
    @Column(name = "BOOKING_START_DATE")
    private LocalDate bookingStartDate; 
    
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketEntity> tickets;
    
    @Column(name = "IS_DELECTED", nullable = true)
    private boolean isDeleted = true; // Cờ đánh dấu trạng thái xóa mềm

}
