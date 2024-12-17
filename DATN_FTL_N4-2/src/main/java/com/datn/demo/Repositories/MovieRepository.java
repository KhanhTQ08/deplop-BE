
package com.datn.demo.Repositories;

import com.datn.demo.DTO.MovieStatisticDTO;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {
	  @Query("SELECT m FROM MovieEntity m WHERE " +
	           "LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(m.genre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
	           "LOWER(m.actor) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	    List<MovieEntity> findByKeywordContainingIgnoreCase(@Param("keyword") String keyword);
	  boolean existsByMovieName(String movieName);
    Page<MovieEntity> findAll(Pageable pageable);
	List<MovieEntity> findByIsDeletedFalseOrderByMovieIdDesc();

	List<MovieEntity> findByIsDeletedTrueOrderByMovieIdDesc();	
	MovieEntity findByMovieId(int movieId);

    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) "
            + "FROM InvoiceEntity i " + "JOIN i.showtime s " + "JOIN s.movie m " + "GROUP BY m.movieName")
    List<MovieStatisticDTO> findMovieStatistics();

    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.movie m " +
            "WHERE CAST(i.bookingDate AS DATE) = CAST(GETDATE() AS DATE) " +
            "GROUP BY m.movieName")
    List<MovieStatisticDTO> findMovieStatisticsToday();


    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.movie m " +
            "GROUP BY m.movieName " +
            "ORDER BY COUNT(i.invoiceId) DESC")
    List<MovieStatisticDTO> findTop3Movies();
    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.movie m " +
            "GROUP BY m.movieName")
    List<MovieStatisticDTO> findAllMoviesInInvoices();
    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.movie m " +
            "WHERE i.bookingDate BETWEEN :startDate AND :endDate " +
            "GROUP BY m.movieName")
    List<MovieStatisticDTO> findMoviesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT new com.datn.demo.DTO.MovieStatisticDTO(m.movieName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.movie m " +
            "WHERE CAST(i.bookingDate AS DATE) = CAST(:filterDate AS DATE) " +
            "GROUP BY m.movieName")
    List<MovieStatisticDTO> findMovieStatisticsByDate(@Param("filterDate") LocalDate filterDate);

    // Tìm kiếm đạo diễn
    @Query("SELECT DISTINCT m.director FROM MovieEntity m WHERE m.director LIKE %:query%")
    List<String> findDirectorsByQuery(@Param("query") String query);

    // Tìm kiếm diễn viên
    @Query("SELECT DISTINCT m.actor FROM MovieEntity m WHERE m.actor LIKE %:query%")
    List<String> findActorsByQuery(@Param("query") String query);

    // Tìm kiếm thể loại
    @Query("SELECT DISTINCT m.genre FROM MovieEntity m WHERE m.genre LIKE %:query%")
    List<String> findGenresByQuery(@Param("query") String query);

}
