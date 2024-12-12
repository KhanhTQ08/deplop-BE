package com.datn.demo.Entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "MOVIE")
public class MovieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIE_ID")
    private int movieId; // Mã phim

    @Column(name = "MOVIE_NAME", nullable = false, length = 255)
    private String movieName; // Tên phim

    @Column(name = "TRAILER_URL", columnDefinition = "NVARCHAR(MAX)")
    private String trailerUrl; // Đường dẫn trailer

    @Column(name = "CONTENT", columnDefinition = "NVARCHAR(MAX)")
    private String content; // Nội dung phim

    @Column(name = "AGE_RESTRICTION", length = 50)
    private String ageRestriction; // Giới hạn độ tuổi

    @Column(name = "DURATION")
    private int duration; // Thời gian phim (phút)

    @Column(name = "DIRECTOR", length = 255)
    private String director; // Đạo diễn

    @Column(name = "ACTOR", length = 255)
    private String actor; // Diễn viên

    @Column(name = "IMAGE", columnDefinition = "NVARCHAR(MAX)")
    private String image; // Hình ảnh phim

    @Column(name = "GENRE", length = 255)
    private String genre; // Thể loại phim

    @Column(name = "IMAGE_BG", length = 255)
    private String image_bg; // Hình nền phim

    @Column(name = "IS_DELECTED", nullable = true)
    private boolean isDeleted = true; // Cờ đánh dấu trạng thái xóa mềm
  
    @OneToMany(mappedBy = "movie")
    @JsonIgnore
    private List<ShowtimeEntity> showtimes;
}
