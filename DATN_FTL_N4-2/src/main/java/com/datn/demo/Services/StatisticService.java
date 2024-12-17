package com.datn.demo.Services;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.demo.DTO.CinemaStatisticDTO;
import com.datn.demo.DTO.MovieStatisticDTO;
import com.datn.demo.Repositories.CinemaInformationRepository;
import com.datn.demo.Repositories.InvoiceRepository;
import com.datn.demo.Repositories.MovieRepository;

@Service

public class StatisticService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaInformationRepository cinemaInformationRepository;

    public List<MovieStatisticDTO> getMovieStatistics() {
        return movieRepository.findMovieStatistics();
    }

    public List<CinemaStatisticDTO> getCinemaStatistics() {
        return cinemaInformationRepository.findCinemaStatistics();
    }
    public List<MovieStatisticDTO> getMovieStatisticsToday() {
        return movieRepository.findMovieStatisticsToday();
    }
    public List<MovieStatisticDTO> getAllMoviesInInvoices() {
        return movieRepository.findAllMoviesInInvoices();
    }

 
    public List<MovieStatisticDTO> getTop3Movies() {
        return movieRepository.findTop3Movies().stream().limit(3).collect(Collectors.toList());
    }
    public BigDecimal getTotalRevenueByMonth() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        BigDecimal totalRevenue = invoiceRepository.findTotalRevenueByMonth(currentMonth, currentYear);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyFormatter.format(amount);
    }
    public List<MovieStatisticDTO> getMoviesByDateRange(LocalDate startDate, LocalDate endDate) {
        return movieRepository.findMoviesByDateRange(startDate, endDate);
    }
    public List<MovieStatisticDTO> getTop3MoviesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<MovieStatisticDTO> movies = movieRepository.findMoviesByDateRange(startDate, endDate);
        return movies.stream()
                     .sorted(Comparator.comparingLong(MovieStatisticDTO::getTicketCount).reversed())
                     .limit(3)
                     .collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueForAllCinemas() {
        BigDecimal totalRevenue = cinemaInformationRepository.findTotalRevenueForAllCinemas();
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public Long getTotalTicketsForAllCinemas() {
        Long totalTickets = cinemaInformationRepository.findTotalTicketsForAllCinemas();
        return totalTickets != null ? totalTickets : 0L;
    }

    public List<CinemaStatisticDTO> getCinemasByDateRange(LocalDate startDate, LocalDate endDate) {
        return cinemaInformationRepository.findCinemasByDateRange(startDate, endDate);
    }


}