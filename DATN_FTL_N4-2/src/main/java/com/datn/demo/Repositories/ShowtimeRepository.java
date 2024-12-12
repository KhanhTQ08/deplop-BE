package com.datn.demo.Repositories;

import com.datn.demo.DTO.FullMovieShowtimeDTO;
import com.datn.demo.DTO.ShowtimeDetailsDTO;
import com.datn.demo.Entities.ShowtimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<ShowtimeEntity, Integer> {

	@Query("SELECT new com.datn.demo.DTO.ShowtimeDetailsDTO(s.showtimeId, m.movieName, m.ageRestriction, s.startTime, s.endTime, s.showDate, r.roomName, m.movieId, m.genre, m.content, m.image, m.director, m.actor, m.image_bg, m.trailerUrl) "
			+ "FROM ShowtimeEntity s " + "JOIN s.movie m " + "JOIN s.room r " + "WHERE s.showDate >= CURRENT_DATE")
	List<ShowtimeDetailsDTO> findAllShowtimeDetails();

	@Query("SELECT s FROM ShowtimeEntity s JOIN FETCH s.movie WHERE s.showDate = :date")
	List<ShowtimeEntity> findByShowDate(@Param("date") LocalDate date);

	@Query("SELECT s FROM ShowtimeEntity s JOIN FETCH s.movie WHERE s.showDate BETWEEN :startDate AND :endDate")
	List<ShowtimeEntity> findAllByShowDateBetween(@Param("startDate") LocalDate startDate,
												  @Param("endDate") LocalDate endDate);

	ShowtimeEntity findByShowtimeId(int showtimeId);
    Optional<ShowtimeEntity> findFirstByCinemaInformation_CinemaIdOrderByShowDateAsc(int cinemaId);

	// Truy vấn các ca chiếu theo movieId, sắp xếp theo ngày chiếu (showDate)
	List<ShowtimeEntity> findByMovieMovieIdOrderByShowDate(Integer movieId);
	  List<ShowtimeEntity> findByMovie_MovieId(int movieId);

	@Query("SELECT s FROM ShowtimeEntity s WHERE s.cinemaInformation.cinemaId = :cinemaId AND s.showDate = :date")
	List<ShowtimeEntity> findShowtimesByCinemaAndDate(@Param("cinemaId") int cinemaId, @Param("date") LocalDate date);

	List<ShowtimeEntity> findByIsDeletedFalseOrderByShowtimeIdDesc();

	List<ShowtimeEntity> findByIsDeletedTrueOrderByShowtimeIdDesc();

	@Query("SELECT new com.datn.demo.DTO.FullMovieShowtimeDTO(" +
	        "m.movieId, m.movieName, m.trailerUrl, m.content, m.ageRestriction, m.duration, m.director, m.actor, m.image, m.genre, " +
	        "st.showtimeId, st.showDate, st.startTime, st.endTime, st.originalShowDate, st.reason, " + // Thêm dấu phẩy vào đây
	        "r.roomId, r.roomName, " +
	        "ci.cinemaId, ci.cinemaName, ci.address, ci.phoneNumber, ci.email) " +
	        "FROM ShowtimeEntity st " +
	        "JOIN st.movie m " +
	        "JOIN st.room r " +
	        "JOIN st.cinemaInformation ci")
	List<FullMovieShowtimeDTO> findAllShowtimeCineMovieRoom();

	
	List<ShowtimeEntity> findByCinemaInformation_CinemaId(int cinemaId);
    List<ShowtimeEntity> findByCinemaInformation_CinemaIdAndShowDate(int cinemaId, LocalDate showDate);
}
