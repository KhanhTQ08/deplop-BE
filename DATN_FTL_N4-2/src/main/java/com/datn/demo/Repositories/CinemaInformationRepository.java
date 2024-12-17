package com.datn.demo.Repositories;


import com.datn.demo.DTO.CinemaStatisticDTO;
import com.datn.demo.Entities.CinemaInformationEntity;
import com.datn.demo.Entities.ShowtimeEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaInformationRepository extends JpaRepository<CinemaInformationEntity, Integer> {

    // Additional query methods can be added here if needed

    @Query("SELECT c FROM CinemaInformationEntity c JOIN c.showtimes s WHERE s.showtimeId = :showtimeId")
    CinemaInformationEntity findCinemaByShowtimeId(@Param("showtimeId") Integer showtimeId);

    @Query("SELECT new com.datn.demo.DTO.CinemaStatisticDTO(c.cinemaName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.cinemaInformation c " +
            "GROUP BY c.cinemaName")
    List<CinemaStatisticDTO> findCinemaStatistics();

	List<CinemaInformationEntity> findByStatusTrueOrderByCinemaIdAsc();

    
    @Query("SELECT SUM(i.totalAmount) FROM InvoiceEntity i JOIN i.showtime s JOIN s.cinemaInformation c")
    BigDecimal findTotalRevenueForAllCinemas();

    @Query("SELECT COUNT(i.invoiceId) FROM InvoiceEntity i JOIN i.showtime s JOIN s.cinemaInformation c")
    Long findTotalTicketsForAllCinemas();

    @Query("SELECT new com.datn.demo.DTO.CinemaStatisticDTO(c.cinemaName, COUNT(i.invoiceId), SUM(i.totalAmount)) " +
            "FROM InvoiceEntity i " +
            "JOIN i.showtime s " +
            "JOIN s.cinemaInformation c " +
            "WHERE i.bookingDate BETWEEN :startDate AND :endDate " +
            "GROUP BY c.cinemaName")
    List<CinemaStatisticDTO> findCinemasByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);



    
    boolean existsByCinemaName(String cinemaName);
    
    boolean existsByCinemaNameAndCinemaIdNot(String cinemaName, Integer cinemaId);
    
    //Tìm các rạp có dữ liệu status là true
    List<CinemaInformationEntity> findByStatusTrue();
    //Tìm các rạp có dữ liệu status là false
    List<CinemaInformationEntity> findByStatusFalse();
}