package com.datn.demo.Controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.demo.DTO.ShowtimeDTO;
import com.datn.demo.DTO.ShowtimeDetailsDTO;
import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;
import com.datn.demo.Services.MovieService;
import com.datn.demo.Services.ShowtimeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ConsoleController {
	@Autowired
	private MovieService movieService;

	@Autowired
	private ShowtimeService showtimeService;
	
	
	
	@GetMapping("/movies-with-showtimes")
	public String listMoviesWithShowtimes(Model model,HttpSession session) {
	    // Lấy danh sách tất cả các showtime
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");

		    // Kiểm tra nếu đã đăng nhập và là admin
		    if (acc != null && acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
		        return "redirect:/printError"; // Trả về trang 404 nếu là admin
		    }
	    List<ShowtimeDetailsDTO> showTimes = showtimeService.getAllShowtime();

	    // Tạo một tập hợp để lưu các movieId có ca chiếu
	    Set<Integer> movieIdsWithShowtimes = new HashSet<>();
	    for (ShowtimeDetailsDTO showtime : showTimes) {
	        movieIdsWithShowtimes.add(showtime.getMovieId());
	    }

	    // Lấy tất cả phim và lọc danh sách phim để chỉ giữ lại những phim có ca chiếu
	    List<MovieEntity> allMovies = movieService.getAllMovies().stream()
		        .filter(movie -> movie.isDeleted()) // Lọc phim chưa xóa (isDeleted = true)
		        .collect(Collectors.toList());
	    List<MovieEntity> moviesWithShowtimes = allMovies.stream()
	            .filter(movie -> movieIdsWithShowtimes.contains(movie.getMovieId()))
	            .collect(Collectors.toList());

	    // Cập nhật model với danh sách phim duy nhất và tất cả ca chiếu
	    model.addAttribute("movies", moviesWithShowtimes); // Danh sách phim không trùng
	    model.addAttribute("showtimes", showTimes); // Danh sách tất cả ca chiếu
	    return "main/user/movie_now_showing"; // Tên file HTML
	}


	@GetMapping("/movies-without-showtimes")
	public String listMoviesWithoutShowtimes(Model model, HttpSession session) {
		// Lấy danh sách tất cả các showtime
		
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");

		    // Kiểm tra nếu đã đăng nhập và là admin
		    if (acc != null && acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
		        return "redirect:/printError"; // Trả về trang 404 nếu là admin
		    }
		List<ShowtimeDetailsDTO> showTimes = showtimeService.getAllShowtime();

		// Tạo một tập hợp để lưu các movieId có showtime
		Set<Integer> movieIdsWithShowtimes = new HashSet<>();
		for (ShowtimeDetailsDTO showtime : showTimes) {
			movieIdsWithShowtimes.add(showtime.getMovieId());
		}

		// Lấy tất cả phim
		List<MovieEntity> allMovies = movieService.getAllMovies().stream()
		        .filter(movie -> movie.isDeleted()) // Lọc phim chưa xóa (isDeleted = true)
		        .collect(Collectors.toList());
		// Lọc danh sách phim để chỉ giữ lại những phim không có showtime
		List<MovieEntity> moviesWithoutShowtimes = allMovies.stream()
				.filter(movie -> !movieIdsWithShowtimes.contains(movie.getMovieId())).collect(Collectors.toList());

		// Cập nhật model với danh sách phim không có ca chiếu
		model.addAttribute("movies", moviesWithoutShowtimes);
		model.addAttribute("showtimes", new ArrayList<>(showTimes)); // Gửi danh sách showtime

		return "main/user/movie_up_coming"; // Tên file HTML để hiển thị danh sách phim không có ca chiếu
	}


}
