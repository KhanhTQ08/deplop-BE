package com.datn.demo.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "GENRE")
public class GenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GENRE_ID")
    private Integer genreId;  // ID thể loại phim

    @Column(name = "GENRE_NAME", nullable = false)
    private String genreName;  // Tên thể loại phim
    

    @Column(name = "MOVIE_ID", nullable = false)
    private Integer movieId;  // Tên thể loại phim

}