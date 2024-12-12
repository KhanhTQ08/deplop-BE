package com.datn.demo.Controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.demo.Entities.CinemaInformationEntity;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;
import com.datn.demo.Services.CinemaInformationService;
import com.datn.demo.Services.ShowtimeService;

@Controller
@RequestMapping("/cinemas")
public class CinemaController {

    @Autowired
    private CinemaInformationService cinemaService;

    @Autowired
    private ShowtimeService showtimeService;

    // Lấy danh sách rạp
    @GetMapping
    public String getAllCinemas(Model model) {
        List<CinemaInformationEntity> cinemas = cinemaService.getAllCinemas();
        model.addAttribute("cinemas", cinemas);
        return "main/user/cinema_list"; // Giao diện chung
    }


    @GetMapping("/{cinemaId}/next7days")
    @ResponseBody
    public Map<String, Object> getScheduleForNext7Days(@PathVariable int cinemaId) {
        LocalDate today = LocalDate.now();
        List<LocalDate> next7Days = IntStream.rangeClosed(0, 6)
                .mapToObj(today::plusDays)
                .collect(Collectors.toList());

        List<ShowtimeEntity> allShowtimes = showtimeService.getShowtimesByCinema(cinemaId);
        Map<LocalDate, List<ShowtimeEntity>> showtimesByDate = allShowtimes.stream()
                .collect(Collectors.groupingBy(ShowtimeEntity::getShowDate));

        CinemaInformationEntity cinema = cinemaService.getCinemaByIds(cinemaId);

        // Tạo đối tượng trả về bao gồm thông tin về rạp và lịch chiếu
        Map<String, Object> response = new HashMap<>();
        response.put("cinemaName", cinema.getCinemaName());
        response.put("cinemaAddress", cinema.getAddress());
        
        List<Map<String, Object>> schedule = new ArrayList<>();
        for (LocalDate date : next7Days) {
            Map<String, Object> daySchedule = new HashMap<>();
            daySchedule.put("date", date.toString());

            // Chỉ lấy thông tin cần thiết về suất chiếu
            List<Map<String, Object>> showtimeDetails = showtimesByDate.getOrDefault(date, Collections.emptyList()).stream()
                .map(showtime -> {
                    Map<String, Object> showtimeDetail = new HashMap<>();
                    showtimeDetail.put("showtimeId", showtime.getShowtimeId());
                    showtimeDetail.put("room", showtime.getRoom().getRoomName());
                    return showtimeDetail;
                })
                .collect(Collectors.toList());

            daySchedule.put("showtimes", showtimeDetails);
            schedule.add(daySchedule);
        }

        response.put("schedule", schedule);
        return response;
    }


    // API lấy danh sách phim theo ngày
    @GetMapping("/{cinemaId}/movies/{date}")
    @ResponseBody
    public List<MovieEntity> getMoviesByDate(@PathVariable int cinemaId, @PathVariable String date) {
        LocalDate showDate = LocalDate.parse(date);
        List<ShowtimeEntity> showtimes = showtimeService.getShowtimesByCinemaAndDate(cinemaId, showDate);
        return showtimes.stream()
                .map(ShowtimeEntity::getMovie)
                .distinct()
                .collect(Collectors.toList());
    }
}
