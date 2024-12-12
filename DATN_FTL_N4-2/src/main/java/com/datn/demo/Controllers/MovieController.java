package com.datn.demo.Controllers;

import com.datn.demo.DTO.ShowtimeDetailsDTO;
import com.datn.demo.Entities.*;
import com.datn.demo.Services.CinemaInformationService;
import com.datn.demo.Services.MovieService;
import com.datn.demo.Services.ReviewService;
import com.datn.demo.Services.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/index")
public class MovieController {
	@Autowired
	private MovieService movieService;

	@Autowired
	private ShowtimeService showtimeService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private CinemaInformationService cinemaInformationService;

	@GetMapping("/search")
	public String searchMovieByKeyword(@RequestParam("keyword") String keyword, Model model) {
	    try {
	        // Tìm kiếm phim theo từ khóa
	        List<MovieEntity> movies = movieService.findMoviesByKeyword(keyword);

	        // Truyền từ khóa và danh sách phim vào model
	        model.addAttribute("searchKeyword", keyword);
	        model.addAttribute("movies", movies);

	        if (!movies.isEmpty()) {
	            model.addAttribute("resultCount", movies.size());
	            return "main/user/findmovie";
	        } else {
	            model.addAttribute("message", "Rất tiếc! Không tìm thấy phim nào phù hợp.");
	            return "main/user/no_movie_found";
	        }
	    } catch (Exception e) {
	        model.addAttribute("message", "Đã xảy ra lỗi!");
	        model.addAttribute("movies", Collections.emptyList());
	        return "main/user/findmovie";
	    }
	}

	@GetMapping("/api/searchMovies")
	@ResponseBody
	public List<MovieEntity> searchMoviesByKeyword(@RequestParam("query") String query) {
	    return movieService.findMoviesByKeyword(query);
	}

	// Hiển thị danh sách phim
	@GetMapping
	public String listMovies(Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, HttpSession session) {
		// Giới hạn số lượng phim trên mỗi trang là 10
		// Lấy danh sách tất cả các showtime
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");

		    // Kiểm tra nếu đã đăng nhập và là admin
		    if (acc != null && acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
		        return "redirect:/printError"; // Trả về trang 404 nếu là admin
		    }
		List<ShowtimeDetailsDTO> showTimes = showtimeService.getAllShowtime();

		// Gộp dữ liệu showtimes theo movieId
		Map<Integer, ShowtimeDetailsDTO> uniqueShowtimesMap = new HashMap<>();

		for (ShowtimeDetailsDTO showtime : showTimes) {
			if (!uniqueShowtimesMap.containsKey(showtime.getMovieId())) {
				uniqueShowtimesMap.put(showtime.getMovieId(), showtime);
			} else {
				// Nếu movieId đã tồn tại, có thể cập nhật thông tin khác nếu cần
				ShowtimeDetailsDTO existingShowtime = uniqueShowtimesMap.get(showtime.getMovieId());
				// Nếu có thêm thông tin khác muốn gộp, có thể thêm ở đây
			}
		}

// Chuyển đổi Map thành danh sách
		List<ShowtimeDetailsDTO> uniqueShowtimes = new ArrayList<>(uniqueShowtimesMap.values());

		// Tạo một tập hợp để lưu các movieId có showtime
		Set<Integer> movieIdsWithShowtimes = showTimes.stream().map(ShowtimeDetailsDTO::getMovieId)
				.collect(Collectors.toSet());

		// Lấy tất cả phim
		List<MovieEntity> allMovies = movieService.getAllMovies().stream()
		        .filter(movie -> movie.isDeleted()) // Lọc phim chưa xóa (isDeleted = true)
		        .collect(Collectors.toList());

		// Lọc danh sách phim để chỉ giữ lại những phim không có showtime
		List<MovieEntity> moviesWithoutShowtimes = allMovies.stream()
				.filter(movie -> !movieIdsWithShowtimes.contains(movie.getMovieId())).collect(Collectors.toList());

		// Tính toán tổng số trang
		int totalMovies = moviesWithoutShowtimes.size();
		int totalPages = (int) Math.ceil((double) totalMovies / size);

		// Lấy danh sách phim đã phân trang
		int startItem = page * size;
		List<MovieEntity> paginatedMovies = moviesWithoutShowtimes.stream().skip(startItem).limit(size)
				.collect(Collectors.toList());

		// Cập nhật model với danh sách phim đã phân trang
		model.addAttribute("movies", paginatedMovies);
		/*
		 * model.addAttribute("showtimes", new ArrayList<>(showTimes)); // Gửi danh sách
		 * showtime
		 */
		model.addAttribute("showtimes", uniqueShowtimes);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		
		return "main/index"; // Tên file HTML để hiển thị danh sách phim
	}
	@GetMapping("/api/movies")
	@ResponseBody
	public Map<String, Object> getMoviesWithPagination(
	    @RequestParam(defaultValue = "0") int page, 
	    @RequestParam(defaultValue = "10") int size) {

	    // Lấy tất cả showtimes
	    List<ShowtimeDetailsDTO> showTimes = showtimeService.getAllShowtime();

	    // Gộp dữ liệu showtimes theo movieId (chỉ giữ lại phim có một ca chiếu duy nhất)
	    Set<Integer> movieIdsWithShowtimes = showTimes.stream()
	            .map(ShowtimeDetailsDTO::getMovieId)
	            .collect(Collectors.toSet());

	    // Lấy tất cả phim
	    List<MovieEntity> allMovies = movieService.getAllMovies().stream()
	            .filter(movie -> movie.isDeleted()) // Lọc phim chưa xóa (isDeleted = true)
	            .collect(Collectors.toList());

	    // Lọc danh sách phim để chỉ giữ lại những phim không có showtime
	    List<MovieEntity> moviesWithoutShowtimes = allMovies.stream()
	            .filter(movie -> !movieIdsWithShowtimes.contains(movie.getMovieId()))
	            .collect(Collectors.toList());

	    // Tính toán tổng số trang
	    int totalMovies = moviesWithoutShowtimes.size();
	    int totalPages = (int) Math.ceil((double) totalMovies / size);

	    // Lấy danh sách phim đã phân trang
	    int startItem = page * size;
	    List<MovieEntity> paginatedMovies = moviesWithoutShowtimes.stream()
	            .skip(startItem)
	            .limit(size)
	            .collect(Collectors.toList());

	    // Trả về dữ liệu dạng Map
	    Map<String, Object> response = new HashMap<>();
	    response.put("movies", paginatedMovies);
	    response.put("currentPage", page);
	    response.put("totalPages", totalPages);

	    return response;
	}


	// Hiển thị form thêm phim
	@GetMapping("/add")
	public String showAddMovieForm(Model model) {
		model.addAttribute("movie", new MovieEntity());
		return "add_movie"; // Tên file HTML để hiển thị form thêm phim
	}

	// Xử lý thêm phim
	@PostMapping("/add")
	public String addMovie(@ModelAttribute MovieEntity movie) {
		movieService.saveMovie(movie);
		return "redirect:/index"; // Quay lại danh sách phim sau khi thêm
	}

	// Xóa phim
	@GetMapping("/delete/{id}")
	public String deleteMovie(@PathVariable int id) {
		movieService.deleteMovie(id);
		return "redirect:/index"; // Quay lại danh sách phim sau khi xóa
	}

	@GetMapping("/details/{id}")
	public String getMovieDetails(@PathVariable("id") int movieId, Model model, HttpSession session) {
		Optional<MovieEntity> movieOpt = movieService.getMovieById(movieId);
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");

		    // Kiểm tra nếu đã đăng nhập và là admin
		    if (acc != null && acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
		        return "redirect:/printError"; // Trả về trang 404 nếu là admin
		    }
		if (movieOpt.isPresent()) {
			// Lấy danh sách ca chiếu theo movieId
			List<ShowtimeEntity> showtimes = showtimeService.getShowtimesByMovieId(movieId);

			// Lọc các ca chiếu có ngày chiếu lớn hơn ngày hiện tại
		     List<ShowtimeEntity> filteredShowtimes = showtimes.stream()
			            .filter(showtime -> {
			                boolean isBookingStartDateValid = (showtime.getBookingStartDate() == null || !LocalDate.now().isBefore(showtime.getBookingStartDate()));
			                boolean isValidShowtime = showtime.getShowDate().isAfter(LocalDate.now()) || 
			                                          (showtime.getShowDate().isEqual(LocalDate.now()) && showtime.getStartTime().isAfter(LocalTime.now()));
			                boolean isNotDeleted = showtime.isDeleted(); // Kiểm tra xem suất chiếu có bị xóa mềm không

			                return isBookingStartDateValid && isValidShowtime && isNotDeleted; // Thêm điều kiện kiểm tra xóa mềm
			            })
			            
			            .collect(Collectors.toList());

			        model.addAttribute("showtimes", filteredShowtimes);
			        LocalDate today = LocalDate.now(); // Ngày hiện tại

			     // Tạo Map chỉ chứa các ngày dự kiến đặt vé > hôm nay
			     Map<LocalDate, List<String>> bookingDatesWithCinemas = showtimes.stream()
			         .filter(showtime -> showtime.getBookingStartDate() != null)
			         .collect(Collectors.groupingBy(
			             ShowtimeEntity::getBookingStartDate, // Nhóm theo ngày mở bán
			             Collectors.mapping(
			                 showtime -> showtime.getCinemaInformation().getCinemaName(), // Lấy tên rạp
			                 Collectors.toList() // Gom các rạp vào danh sách
			             )
			         ))
			         .entrySet()
			         .stream()
			         .filter(entry -> entry.getKey().isAfter(today)) // Chỉ giữ ngày dự kiến đặt vé > hôm nay
			         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Chuyển lại thành Map

			     // Đưa dữ liệu đã lọc vào model
			     model.addAttribute("bookingDatesWithCinemas", bookingDatesWithCinemas);


			// Lấy danh sách tất cả phim để hiển thị bên phải
			List<MovieEntity> movies2 = movieService.getAllMovies();
			model.addAttribute("movie", movieOpt.get());
			model.addAttribute("movieDetails", movies2); // Thêm danh sách phim vào model

			// Đặt tên phim làm tiêu đề
			String titleMovieName = movieOpt.get().getMovieName();
			model.addAttribute("title", titleMovieName);

			// Lấy danh sách các showtime
			List<ShowtimeDetailsDTO> showTimes = showtimeService.getAllShowtime();

			// Gộp dữ liệu showtimes theo movieId
			Map<Integer, ShowtimeDetailsDTO> uniqueShowtimesMap = new HashMap<>();

			for (ShowtimeDetailsDTO showtime : showTimes) {
				if (!uniqueShowtimesMap.containsKey(showtime.getMovieId())) {
					uniqueShowtimesMap.put(showtime.getMovieId(), showtime);
				} else {
					// Nếu movieId đã tồn tại, có thể cập nhật thông tin khác nếu cần
					ShowtimeDetailsDTO existingShowtime = uniqueShowtimesMap.get(showtime.getMovieId());
					// Nếu có thêm thông tin khác muốn gộp, có thể thêm ở đây
				}
			}
			List<ShowtimeDetailsDTO> uniqueShowtimes = new ArrayList<>(uniqueShowtimesMap.values());
			model.addAttribute("showtimeDetails", uniqueShowtimes);

			List<ReviewEntity> reviews = reviewService.getReviewsByMovie(movieOpt.get());
			Collections.reverse(reviews);
			model.addAttribute("reviews", reviews);
			  List<CinemaInformationEntity> cinemas = cinemaInformationService.getAllCinemas();
		        model.addAttribute("cinemas", cinemas);
			// Chỉ lấy các rạp chiếu có suất chiếu của phim đang xem
			List<CinemaInformationEntity> cinemaAll = cinemaInformationService.getCinemasWithShowtimesByMovie(movieId);
			// Sắp xếp danh sách rạp theo số lượng suất chiếu giảm dần
			cinemaAll.sort((cinema1, cinema2) -> {
				int count1 = cinema1.getShowtimes().size();
				int count2 = cinema2.getShowtimes().size();
				return Integer.compare(count2, count1); // Sắp xếp giảm dần
			});

			// Thêm danh sách đã sắp xếp vào model
			model.addAttribute("cinemaAll", cinemaAll);
			return "main/user/chi_tiet_phim";
		} else {
			return "redirect:/index";
		}
	}
	@GetMapping("/{cinemaId}/next7days")
	@ResponseBody
	public List<Map<String, Object>> getScheduleForNext7Days(@PathVariable int cinemaId, HttpSession session) {
	    Integer accId = (Integer) session.getAttribute("accId");

	    LocalDate today = LocalDate.now();
	    LocalDate bookingStartDate = showtimeService.getBookingStartDate(cinemaId);

	    // Nếu ngày hiện tại trước ngày bắt đầu đặt vé, bắt đầu từ ngày đặt vé
	    LocalDate startDate = today.isBefore(bookingStartDate) ? bookingStartDate : today;

	    List<LocalDate> next7Days = IntStream.range(0, 7)
	        .mapToObj(startDate::plusDays)
	        .collect(Collectors.toList());

	    List<ShowtimeEntity> allShowtimes = showtimeService.getShowtimesByCinema(cinemaId);
	    Map<LocalDate, List<ShowtimeEntity>> showtimesByDate = allShowtimes.stream()
	        .collect(Collectors.groupingBy(ShowtimeEntity::getShowDate));

	    List<Map<String, Object>> schedule = new ArrayList<>();
	    for (LocalDate date : next7Days) {
	        Map<String, Object> daySchedule = new HashMap<>();
	        daySchedule.put("date", date.toString());

	        List<Map<String, Object>> showtimeDetails = showtimesByDate.getOrDefault(date, Collections.emptyList())
	            .stream()
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

	    return schedule;
	}



	@GetMapping("/{cinemaId}/movies/{date}")
	@ResponseBody
	public List<MovieEntity> getMoviesByDate(@PathVariable int cinemaId, @PathVariable String date, HttpSession session) {
	    Integer accId = (Integer) session.getAttribute("accId");

	    LocalDate showDate = LocalDate.parse(date);
	    LocalDate bookingStartDate = showtimeService.getBookingStartDate(cinemaId);

	    // Nếu chưa đến ngày đặt vé thì trả về danh sách rỗng
	    if (bookingStartDate != null && showDate.isBefore(bookingStartDate)) {
	        return Collections.emptyList();
	    }

	    List<ShowtimeEntity> showtimes = showtimeService.getShowtimesByCinemaAndDate(cinemaId, showDate);
	    return showtimes.stream()
	            .map(ShowtimeEntity::getMovie)
	            .distinct()
	            .collect(Collectors.toList());
	}

	@GetMapping("/{cinemaId}/next7days/showtimes/{date}")
	@ResponseBody
	public List<Map<String, Object>> getShowtimesForDay(@PathVariable int cinemaId, @PathVariable String date, HttpSession session) {
	    Integer accId = (Integer) session.getAttribute("accId");

	    LocalDate showDate = LocalDate.parse(date); // Ngày muốn xem suất chiếu
	    LocalDate bookingStartDate = showtimeService.getBookingStartDate(cinemaId); // Ngày bắt đầu đặt vé

	    // Nếu chưa đến ngày đặt vé thì trả về danh sách rỗng
	    if (bookingStartDate != null && showDate.isBefore(bookingStartDate)) {
	        return Collections.emptyList();
	    }

	    ZoneId userZone = ZoneId.systemDefault();
	    LocalDateTime currentTime = LocalDateTime.now(userZone); // Thời gian hiện tại

	    List<ShowtimeEntity> showtimes = showtimeService.getShowtimesByCinemaAndDate(cinemaId, showDate);

	    // Lọc suất chiếu hợp lệ dựa trên thời gian hiện tại và thời gian đặt vé
	    showtimes = showtimes.stream()
	        .filter(showtime -> {
	            // Kiểm tra nếu ngày bắt đầu đặt vé có tồn tại và nếu thời gian hiện tại chưa qua ngày bắt đầu đặt vé
	            LocalDate bookingStartDateForShowtime = showtime.getBookingStartDate();
	            if (bookingStartDateForShowtime != null && showDate.isEqual(bookingStartDateForShowtime) && currentTime.toLocalDate().isBefore(bookingStartDateForShowtime)) {
	                return false; // Nếu chưa đến ngày bắt đầu đặt vé, không hiển thị suất chiếu
	            }

	            // Kiểm tra nếu suất chiếu phải lớn hơn thời gian hiện tại + 30 phút
	            LocalDateTime showtimeStartDateTime = showDate.atTime(showtime.getStartTime());
	            return showtimeStartDateTime.isAfter(currentTime.plusMinutes(30)); // Suất chiếu phải sau 30 phút từ thời gian hiện tại
	        })
	        .collect(Collectors.toList());

	    // Tạo map với key là movieId và giá trị là danh sách các suất chiếu
	    Map<Integer, List<Map<String, Object>>> showtimesByMovie = new HashMap<>();
	    showtimes.forEach(showtime -> {
	        showtimesByMovie.putIfAbsent(showtime.getMovie().getMovieId(), new ArrayList<>());

	        Map<String, Object> showtimeDetail = new HashMap<>();
	        showtimeDetail.put("showtimeId", showtime.getShowtimeId());
	        showtimeDetail.put("room", showtime.getRoom().getRoomName());
	        showtimeDetail.put("startTime", showtime.getStartTime());
	        showtimeDetail.put("endTime", showtime.getEndTime());
	        showtimeDetail.put("bookingStartTime", showtime.getBookingStartDate()); // Thêm thông tin về ngày đặt vé nếu có

	        showtimesByMovie.get(showtime.getMovie().getMovieId()).add(showtimeDetail);
	    });

	    // Chuyển đổi danh sách showtimes thành định dạng JSON
	    return showtimesByMovie.entrySet().stream()
	        .map(entry -> {
	            Map<String, Object> movieSchedule = new HashMap<>();
	            movieSchedule.put("movieId", entry.getKey());
	            movieSchedule.put("showtimes", entry.getValue());
	            return movieSchedule;
	        })
	        .collect(Collectors.toList());
	}

	public String formatText(String text) {
		// Thêm thẻ <br> sau mỗi dấu chấm
		return text.replaceAll("\\.\\s*", ".<br>");
	}
}
