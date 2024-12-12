package com.datn.demo.Controllers;

import java.util.*;

import com.datn.demo.Entities.*;
import com.datn.demo.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.demo.Beans.AccountBean;
import com.datn.demo.Beans.MovieBean;
import com.datn.demo.Beans.ProductBean;
import com.datn.demo.Beans.RoleBean;
import com.datn.demo.Beans.RoomBean;
import com.datn.demo.Beans.ShowtimeBean;
import com.datn.demo.DTO.RoomDTO;
import com.datn.demo.Repositories.AccountRepository;
import com.datn.demo.Repositories.CinemaInformationRepository;
import com.datn.demo.Repositories.MovieRepository;
import com.datn.demo.Repositories.ProductRepository;
import com.datn.demo.Repositories.ReviewRepository;
import com.datn.demo.Repositories.RoleRepository;
import com.datn.demo.Repositories.RoomRepository;
import com.datn.demo.Repositories.ShowtimeRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AdminControler {

	@Autowired
	private MovieService movieService;
	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ShowtimeRepository showtimeRepository;
	@Autowired
	private SeatService seatService;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private CinemaInformationRepository cinemaInformationRepository;
	@Autowired
	private RoomService roomService;
	@Autowired
	private TicketService ticketService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private CinemaInformationService cinemaService;
	@Autowired
	private CinemaInformationRepository cinemaRepository;
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private PrintController printController;
	@Autowired
	private AccountBean accountBean;

	@GetMapping("/cinema/check-duplicate")
	@ResponseBody
	public boolean checkDuplicateCinemaName(@RequestParam String cinemaName, @RequestParam Integer cinemaId) {
		// Kiểm tra nếu có tên rạp trùng nhưng khác ID hiện tại
		return cinemaRepository.existsByCinemaNameAndCinemaIdNot(cinemaName, cinemaId);
	}

	@GetMapping("/cinema")
	public String getCinemas(Model model, HttpSession session) {
	    List<CinemaInformationEntity> cinemas = cinemaService.getAllCinemas(); // Lấy danh sách rạp (status = true)
	   List<CinemaInformationEntity> trashCinemas = cinemaService.getTrashCinemas(); // Lấy danh sách thùng rác (status = false)
	   AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	   if (acc == null) {
		   return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	   } 
	   

	   if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
		   return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	   }
	    model.addAttribute("cinemas", cinemas);
	    model.addAttribute("trashCinemas", trashCinemas); // Truyền danh sách thùng rác vào view
	    model.addAttribute("newCinema", new CinemaInformationEntity());
	    return "admin/components/cinenmas"; // Đường dẫn tới file HTML
	}

	@PostMapping("/cinema/add")
	public String addCinema(@Valid @ModelAttribute("newCinema") CinemaInformationEntity newCinema, BindingResult result,
			RedirectAttributes redirectAttributes, Model model) {

		// Kiểm tra lỗi validation
		if (result.hasErrors()) {
			model.addAttribute("errorMessage", "Vui lòng kiểm tra lại các trường đã điền!"); // Truyền thông báo lỗi
			model.addAttribute("cinemas", cinemaRepository.findAll());
			return "admin/components/cinenmas";
		}

		if (newCinema.getCinemaId() == null) {
			// Kiểm tra trùng tên rạp
			if (cinemaRepository.existsByCinemaName(newCinema.getCinemaName())) {
				result.rejectValue("cinemaName", "error.newCinema", "Tên rạp đã tồn tại!");
				model.addAttribute("errorMessage", "Vui lòng kiểm tra lại các trường đã điền!");
				model.addAttribute("cinemas", cinemaRepository.findAll());
				return "admin/components/cinenmas";
			}
		}

		// Lưu rạp mới nếu không có lỗi
		redirectAttributes.addFlashAttribute("successMessage", "Thêm thành công!");
		cinemaRepository.save(newCinema);
		return "redirect:/cinema";
	}

	@PostMapping("/cinema/save")
	public String saveCinema(@ModelAttribute("newCinema") @Valid CinemaInformationEntity cinema, BindingResult result,
			RedirectAttributes redirectAttributes, Model model) {
		// Kiểm tra lỗi
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("errorUMessage", "Sửa không thành công!");
			model.addAttribute("cinemas", cinemaRepository.findAll());
			return "redirect:/cinema";

		}

		// Kiểm tra nếu có cinemaId (tức là sửa rạp)
		if (cinema.getCinemaId() != null && cinema.getCinemaId() > 0) {
			// Nếu cinemaId đã có (cập nhật)'
			redirectAttributes.addFlashAttribute("cinemaMessage", "Sửa thành công!");

			cinemaRepository.save(cinema); // Cập nhật rạp
		} else {
			// Nếu cinemaId chưa có (thêm mới)
			cinemaRepository.save(cinema); // Thêm mới rạp
		}

		return "redirect:/cinema"; // Sau khi lưu thành công, chuyển hướng đến danh sách rạp
	}

	@GetMapping("/cinema/edit/{id}")
	public String showEditCinemaForm(@PathVariable("id") Integer id, Model model) {
		// Tìm cinema theo ID và đưa vào model
		CinemaInformationEntity cinema = cinemaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid cinema Id:" + id));
		model.addAttribute("newCinema", cinema); // Đối tượng cinema để chỉnh sửa

		List<CinemaInformationEntity> cinemas = cinemaService.getAllCinemas();
		model.addAttribute("cinemas", cinemas);

		return "admin/components/cinenmas"; // Trả về view chứa form chỉnh sửa và danh sách rạp
	}


	@GetMapping("/cinema/deleteF/{id}")
	public String DELETEcinema(@PathVariable int id, RedirectAttributes redirectAttributes) {
	    try {
	        cinemaService.DELETEcinema(id); // Gọi service để xóa rạp
	        redirectAttributes.addFlashAttribute("successUMessage", "Đã xóa rạp thành công!");
	    } catch (Exception e) {
	        // Bắt lỗi khi không xóa được (vd: rạp còn phòng)
	        redirectAttributes.addFlashAttribute("errorDMessage", "Không thể xóa vì còn phòng tồn tại trong rạp!");
	    }
	    return "redirect:/cinema";

	}
	
	@GetMapping("/cinema/delete/{id}")
	public String deleteCinema(@PathVariable int id, RedirectAttributes redirectAttributes) {
	    try {
	        cinemaService.deleteCinema(id); // Gọi service để xóa rạp
	        redirectAttributes.addFlashAttribute("successUMessage", "Đã chuyển trạng thái rạp thành không hoạt động!");
	    } catch (Exception e) {
	        // Bắt lỗi khi không xóa được 
	    	redirectAttributes.addFlashAttribute("errorDMessage", "Có lỗi xảy ra: " + e.getMessage());
	    }
	    return "redirect:/cinema";
	}
	@GetMapping("/cinema/trash")
	public String getTrashCinemas(Model model) {
	    List<CinemaInformationEntity> trashCinemas = cinemaService.getTrashCinemas();
	    model.addAttribute("trashCinemas", trashCinemas);
	    return "admin/components/trash"; // Đường dẫn tới file HTML cho giao diện thùng rác
	}
	
	
	@GetMapping("/cinema/restore/{id}")
	public String restoreCinema(@PathVariable int id, RedirectAttributes redirectAttributes) {
	    try {
	        cinemaService.restoreCinema(id); // Gọi service để khôi phục rạp
	        redirectAttributes.addFlashAttribute("successRMessage", "Đã khôi phục rạp thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorRMessage", "Có lỗi xảy ra: " + e.getMessage());
	    }
	    return "redirect:/cinema";
	}

	@PostMapping("/cinema/reset")
	public String resetCinema(Model model) {

		// Tạo đối tượng CinemaEntity mới với các giá trị rỗng
		CinemaInformationEntity cinema = new CinemaInformationEntity();

		// Đặt giá trị rỗng cho các trường trong đối tượng cinema nếu cần
		cinema.setCinemaId(null); // Có thể để null nếu không cần ID
		cinema.setCinemaName("");
		cinema.setAddress("");
		cinema.setPhoneNumber("");
		cinema.setEmail("");

		model.addAttribute("newCinema", cinema);
		model.addAttribute("cinemas", cinemaRepository.findAll());

		return "demo1/components/cinemas"; // Trả về lại trang form với dữ liệu đã reset
	}
	/*
	 * =============================================================================
	 * ================
	 */

	@GetMapping("/showtime")
	public String listShowtimes(@RequestParam(required = false) Integer cinemaId, Model model,HttpSession session) {
		model.addAttribute("showtimeBean", new ShowtimeBean());
		model.addAttribute("movies", movieRepository.findAll());
		model.addAttribute("cinemas", cinemaInformationRepository.findByStatusTrue());
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		// Nếu không có cinemaId, trả về tất cả phòng
		if (cinemaId == null) {
			model.addAttribute("rooms", roomRepository.findAll());
		} else {
			// Lọc danh sách phòng theo cinemaId
			model.addAttribute("rooms", roomRepository.findByCinema_CinemaId(cinemaId));
		}

		model.addAttribute("showtimes", showtimeRepository.findByIsDeletedTrueOrderByShowtimeIdDesc());
		model.addAttribute("deletedShowtimes", showtimeRepository.findByIsDeletedFalseOrderByShowtimeIdDesc());

		return "admin/components/screening";
	}

	@PostMapping("/add-showtime")
	public String saveShowtime(@Valid @ModelAttribute("showtimeBean") ShowtimeBean showtimeBean, BindingResult error,
			Model model, RedirectAttributes redirectAttributes) {
		if (showtimeBean.getStartTime() != null && showtimeBean.getEndTime() != null
				&& !showtimeBean.getEndTime().isAfter(showtimeBean.getStartTime())) {
			error.rejectValue("endTime", "error.endTime", "Thời gian kết thúc phải sau thời gian bắt đầu.");
		}

		if (showtimeBean.getBookingStartDate() != null
				&& showtimeBean.getBookingStartDate().isAfter(showtimeBean.getShowDate())) {
			error.rejectValue("bookingStartDate", "error.bookingStartDate",
					"Ngày bắt đầu đặt vé phải trước hoặc cùng ngày với ngày chiếu.");
		}
		if (error.hasErrors()) {
			model.addAttribute("rooms", roomRepository.findAll());
			model.addAttribute("movies", movieRepository.findAll());
			model.addAttribute("cinemas", cinemaInformationRepository.findByStatusTrue());
			model.addAttribute("showtimes", showtimeRepository.findAll());
			model.addAttribute("errorMessage", "Vui lòng kiểm tra các trường đã điền.");

			return "admin/components/screening"; // Trả về lại form với lỗi
		}
		// Validate time range
		try {
			showtimeBean.validateTimeRange(error);
		} catch (IllegalArgumentException e) {
			model.addAttribute("timeError", e.getMessage());
			model.addAttribute("rooms", roomRepository.findAll());
			model.addAttribute("movies", movieRepository.findAll());
			model.addAttribute("cinemas", cinemaInformationRepository.findByStatusTrue());
			model.addAttribute("showtimes", showtimeRepository.findAll());
			model.addAttribute("errorMessage", "Vui lòng kiểm tra các trường đã điền.");

			return "admin/components/screening";
		}

		ShowtimeEntity showtime = new ShowtimeEntity();
		showtime.setRoom(roomRepository.findById(showtimeBean.getRoomId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid Room ID")));
		showtime.setMovie(movieRepository.findById(showtimeBean.getMovieId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid Movie ID")));
		showtime.setCinemaInformation(cinemaInformationRepository.findById(showtimeBean.getCinemaId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid Cinema ID")));
		showtime.setShowDate(showtimeBean.getShowDate());
		showtime.setStartTime(showtimeBean.getStartTime());
		showtime.setEndTime(showtimeBean.getEndTime());
		showtime.setBookingStartDate(showtimeBean.getBookingStartDate());
		showtime.setDeleted(true);
		showtimeRepository.save(showtime);
		redirectAttributes.addFlashAttribute("successMessage", "Suất chiếu đã được thêm thành công!"); // Thêm thông báo
																										// thành công

		return "redirect:/showtime";
	}

	// Phản hồi JSON hợp lệ
	@GetMapping("/get-rooms-by-cinema/{cinemaId}")
	@ResponseBody
	public List<Map<String, Object>> getRoomsByCinema(@PathVariable Integer cinemaId) {
		List<RoomEntity> rooms = roomRepository.findByCinema_CinemaId(cinemaId);
		List<Map<String, Object>> response = new ArrayList<>();

		for (RoomEntity room : rooms) {
			Map<String, Object> roomMap = new HashMap<>();
			roomMap.put("roomId", room.getRoomId());
			roomMap.put("roomName", room.getRoomName());
			response.add(roomMap);
		}

		return response;
	}

	@GetMapping("/check-movie-showtime/{movieId}")
	@ResponseBody
	public boolean checkMovieShowtime(@PathVariable Integer movieId) {
		// Kiểm tra xem phim đã có suất chiếu hay chưa
		List<ShowtimeEntity> showtimes = showtimeRepository.findByMovie_MovieId(movieId);
		return !showtimes.isEmpty(); // Trả về true nếu phim đã có suất chiếu
	}

	@PostMapping("/delete-showtime")
	public String deleteShowtime(@RequestParam("showtimeId") Integer showtimeId,
			RedirectAttributes redirectAttributes) {
		ShowtimeEntity showtime = showtimeRepository.findById(showtimeId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Showtime ID"));

		// Đánh dấu suất chiếu là đã bị xóa
		showtime.setDeleted(false);

		// Lưu lại trạng thái mới
		showtimeRepository.save(showtime);

		redirectAttributes.addFlashAttribute("xoaMessage", "Suất chiếu đã được xóa thành công!");
		return "redirect:/showtime"; // Đảm bảo rằng sau khi xóa, bạn luôn tải lại danh sách suất chiếu
	}

	@PostMapping("/restore-showtime/{showtimeId}")
	public String restoreShowtime(@PathVariable Integer showtimeId, RedirectAttributes redirectAttributes) {
		// Tìm suất chiếu theo ID
		ShowtimeEntity showtime = showtimeRepository.findByShowtimeId(showtimeId);

		if (showtime == null) {
			throw new IllegalArgumentException("Invalid Showtime ID");
		}

		// Khôi phục suất chiếu (thay đổi isDeleted thành false)
		showtime.setDeleted(true);

		// Lưu thay đổi vào cơ sở dữ liệu
		showtimeRepository.save(showtime);

		// Thêm thông báo thành công
		redirectAttributes.addFlashAttribute("khoiphucMessage", "Suất chiếu đã được khôi phục!");

		// Quay lại trang danh sách suất chiếu
		return "redirect:/showtime";
	}

	@GetMapping("/account")
	public String listAccounts(Model model,HttpSession session) {
		List<AccountEntity> accounts = accountRepository.findAll();
		List<RoleEntity> roles = roleRepository.findAll();
		
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		
		model.addAttribute("roles", roles);
		model.addAttribute("account", new AccountBean()); // Đảm bảo là tài khoản mới
		model.addAttribute("accounts", accountRepository.findByIsDeletedTrueOrderByAccountIdDesc());
		model.addAttribute("deletedAccount", accountRepository.findByIsDeletedFalseOrderByAccountIdDesc());
		// Thiết lập vai trò mặc định là admin khi tạo tài khoản mới
		AccountBean accountBean = new AccountBean(); // Khởi tạo bean tài khoản mới
		if (accountBean.getAccountId() == 0) {
			accountBean.setRoleId(1); // Giả sử '1' là vai trò admin
		}
		model.addAttribute("account", accountBean);

		return "admin/components/account";
	}

	@PostMapping("/add-account")
	public String saveAccount(@Valid @ModelAttribute("account") AccountBean accountBean, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", roleRepository.findAll());
			model.addAttribute("errorMessage", "Vui lòng kiểm tra các trường đã điền.");
			return "admin/components/account"; // Quay lại form nếu có lỗi
		}

		// Tạo hoặc cập nhật tài khoản
		AccountEntity account = new AccountEntity();
		account.setUsername(accountBean.getUsername());
		account.setFullName(accountBean.getFullName());
		account.setPhoneNumber(accountBean.getPhoneNumber());
		account.setEmail(accountBean.getEmail());
		account.setDeleted(true);
		// Mã hóa mật khẩu nếu tài khoản mới
		if (accountBean.getAccountId() == 0 && !accountBean.getPassword().isEmpty()) {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String encodedPassword = passwordEncoder.encode(accountBean.getPassword());
			account.setPassword(encodedPassword);
		}

		// Kiểm tra vai trò hợp lệ trước khi tìm kiếm
		if (accountBean.getRoleId() != null) {
			RoleEntity role = roleRepository.findById(accountBean.getRoleId()).orElse(null);
			if (role != null) {
				account.setRole(role);
			} else {
				// Xử lý khi không tìm thấy vai trò hợp lệ
				// Ví dụ: đặt vai trò mặc định hoặc hiển thị lỗi
				RoleEntity defaultRole = roleRepository.findById(1).orElse(null); // Giả sử vai trò ID = 1 là Admin
				account.setRole(defaultRole);
			}
		} else {
			// Nếu không có roleId, đặt vai trò mặc định
			RoleEntity defaultRole = roleRepository.findById(1).orElse(null); // Vai trò mặc định là Admin
			account.setRole(defaultRole);
		}
		redirectAttributes.addFlashAttribute("themMessage", "Thêm tài khoản thành công!");
		accountRepository.save(account);
		return "redirect:/account"; // Sau khi thành công, chuyển hướng về danh sách tài khoản
	}

	// Chỉnh sửa tài khoản
	@GetMapping("/edit-account/{id}")
	public String editAccount(@PathVariable("id") int id, Model model) {
		Optional<AccountEntity> account = accountRepository.findById(id);
		if (account.isPresent()) {
			AccountBean accountBean = new AccountBean();
			AccountEntity accountEntity = account.get();

			// Map dữ liệu từ AccountEntity sang AccountBean, không lấy mật khẩu
			accountBean.setAccountId(accountEntity.getAccountId());
			accountBean.setUsername(accountEntity.getUsername());
			accountBean.setFullName(accountEntity.getFullName());
			accountBean.setPhoneNumber(accountEntity.getPhoneNumber());
			accountBean.setEmail(accountEntity.getEmail());
			accountBean.setRoleId(accountEntity.getRole().getRoleId());

			model.addAttribute("account", accountBean);
			model.addAttribute("roles", roleRepository.findAll());
		}
		return "admin/components/account";
	}

	// Cập nhật tài khoản
	@PostMapping("/account-update")
	public String updateAccount(@Valid @ModelAttribute("account") AccountBean accountBean, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", roleRepository.findAll());
			return "admin/components/account"; // Quay lại form với các lỗi
		}

		Optional<AccountEntity> optionalAccount = accountRepository.findById(accountBean.getAccountId());
		if (optionalAccount.isPresent()) {
			AccountEntity account = optionalAccount.get();
			account.setFullName(accountBean.getFullName());
			account.setPhoneNumber(accountBean.getPhoneNumber());
			account.setEmail(accountBean.getEmail());
			account.setRole(roleRepository.findById(accountBean.getRoleId()).orElse(null));
			accountRepository.save(account);
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
		}
		return "redirect:/account"; // Sau khi cập nhật thành công
	}
	@PostMapping("/delete-account")
	public String deleteAccount(@RequestParam("accountId") Integer accountId,
			RedirectAttributes redirectAttributes) {
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Showtime ID"));

		// Đánh dấu suất chiếu là đã bị xóa
		account.setDeleted(false);

		// Lưu lại trạng thái mới
		accountRepository.save(account);

		redirectAttributes.addFlashAttribute("xoaMessage", "Tài khoản đã được vô hiệu hóa thành công!");
		return "redirect:/account"; // Đảm bảo rằng sau khi xóa, bạn luôn tải lại danh sách suất chiếu
	}

	@PostMapping("/restore-account/{accountId}")
	public String restoreAccount(@PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
		// Tìm suất chiếu theo ID
		AccountEntity account = accountRepository.findByAccountId(accountId);

		if (account == null) {
			throw new IllegalArgumentException("Invalid Showtime ID");
		}

		// Khôi phục suất chiếu (thay đổi isDeleted thành false)
		account.setDeleted(true);

		// Lưu thay đổi vào cơ sở dữ liệu
		accountRepository.save(account);

		// Thêm thông báo thành công
		redirectAttributes.addFlashAttribute("khoiphuctaikhoanMessage", "Tài khoản đã được khôi phục!");

		// Quay lại trang danh sách suất chiếu
		return "redirect:/account";
	}

	@GetMapping("/admin")
	public String showTopMoviesPage() {
		return "admin/index";
	}

	@GetMapping("/flim")
	public String movieList(Model model,HttpSession session) {
		// model.addAttribute("movies", movieRepository.findAllByOrderByMovieIdDesc());
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		model.addAttribute("movies", movieRepository.findByIsDeletedTrueOrderByMovieIdDesc());
		model.addAttribute("deletedMovies", movieRepository.findByIsDeletedFalseOrderByMovieIdDesc());
		model.addAttribute("movieBean", new MovieBean()); // Khởi tạo movieBean mặc định
		return "admin/components/flim";
	}

	// Trang thêm phim mới
	@GetMapping("/add-movie")
	public String addMovieForm(Model model) {
		model.addAttribute("movieBean", new MovieBean());
		return "admin/components/flim"; // Sửa lại đường dẫn template
	}

	// Lưu phim mới
	@PostMapping("/add-movie")
	public String addMovieSave(@Valid @ModelAttribute("movieBean") MovieBean movieBean, BindingResult error,
			RedirectAttributes redirectAttributes, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("movies", movieRepository.findAll());
			model.addAttribute("errorMessage", "Vui lòng kiểm tra các trường đã điền.");
			return "admin/components/flim";
		}
		MovieEntity movieEntity = new MovieEntity();
		movieEntity.setMovieName(movieBean.getMovieName());
		movieEntity.setTrailerUrl(movieBean.getTrailerUrl());
		movieEntity.setContent(movieBean.getContent());
		movieEntity.setAgeRestriction(movieBean.getAgeRestriction());
		movieEntity.setDuration(movieBean.getDuration());
		movieEntity.setDirector(movieBean.getDirector());
		movieEntity.setActor(movieBean.getActor());
		movieEntity.setImage(movieBean.getImage());
		movieEntity.setGenre(movieBean.getGenre());
		movieEntity.setImage_bg(movieBean.getImage_bg());
		movieEntity.setDeleted(true);
		movieRepository.save(movieEntity);
		redirectAttributes.addFlashAttribute("successMessage", "Phim đã được thêm thành công!"); // Thêm thông báo thành
																									// công
		return "redirect:/flim";
	}

	// Trang cập nhật thông tin phim
	@GetMapping("/update-movie")
	public String updateMovieForm(@RequestParam("movieId") Integer movieId, Model model) {
		Optional<MovieEntity> optionalMovie = movieRepository.findById(movieId);
		if (optionalMovie.isPresent()) {
			MovieEntity movieEntity = optionalMovie.get();
			MovieBean movieBean = new MovieBean();
			movieBean.setMovieName(movieEntity.getMovieName());
			movieBean.setTrailerUrl(movieEntity.getTrailerUrl());
			movieBean.setContent(movieEntity.getContent());
			movieBean.setAgeRestriction(movieEntity.getAgeRestriction());
			movieBean.setDuration(movieEntity.getDuration());
			movieBean.setDirector(movieEntity.getDirector());
			movieBean.setActor(movieEntity.getActor());
			movieBean.setImage(movieEntity.getImage());
			movieBean.setGenre(movieEntity.getGenre());
			movieBean.setImage_bg(movieEntity.getImage_bg());
			movieBean.setMovieId(movieEntity.getMovieId()); // Set movieId cho trường hợp cập nhật

			model.addAttribute("movieBean", movieBean);
		} else {
			model.addAttribute("movieBean", new MovieBean()); // Khởi tạo mặc định khi không có phim
		}
		model.addAttribute("movies", movieRepository.findAll());
		return "admin/components/flim";
	}

	// Lưu phim sau khi cập nhật
	@PostMapping("/update-movie")
	public String updateMovie(@Valid @ModelAttribute("movieBean") MovieBean movieBean, BindingResult error,
			RedirectAttributes redirectAttributes, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("movies", movieRepository.findAll());
			model.addAttribute("cnpthatbaiMessage", "Cập nhật thất bại!");
			return "admin/components/flim";
		}
		Optional<MovieEntity> optionalMovie = movieRepository.findById(movieBean.getMovieId());
		if (optionalMovie.isPresent()) {
			MovieEntity movieEntity = optionalMovie.get();
			movieEntity.setMovieName(movieBean.getMovieName());
			movieEntity.setTrailerUrl(movieBean.getTrailerUrl());
			movieEntity.setContent(movieBean.getContent());
			movieEntity.setAgeRestriction(movieBean.getAgeRestriction());
			movieEntity.setDuration(movieBean.getDuration());
			movieEntity.setDirector(movieBean.getDirector());
			movieEntity.setActor(movieBean.getActor());
			movieEntity.setImage(movieBean.getImage());
			movieEntity.setGenre(movieBean.getGenre());
			movieEntity.setImage_bg(movieBean.getImage_bg());
			redirectAttributes.addFlashAttribute("phimMessage", "Phim đã được cập nhật thành công!");
			movieRepository.save(movieEntity);
		}
		return "redirect:/flim";
	}

	@PostMapping("/delete-movie")
	public String deleteMovie(@RequestParam("movieId") Integer movieId, RedirectAttributes redirectAttributes) {
		MovieEntity movie = movieRepository.findById(movieId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Movie ID"));

		// Đánh dấu suất chiếu là đã bị xóa
		movie.setDeleted(false);

		// Lưu lại trạng thái mới
		movieRepository.save(movie);

		redirectAttributes.addFlashAttribute("xoaphimMessage", "Phim đã được xóa thành công!");
		return "redirect:/flim"; // Đảm bảo rằng sau khi xóa, bạn luôn tải lại danh sách suất chiếu
	}

	@PostMapping("/restore-movie/{movieId}")
	public String restoreMovie(@PathVariable Integer movieId, RedirectAttributes redirectAttributes) {
		// Tìm suất chiếu theo ID
		MovieEntity movie = movieRepository.findByMovieId(movieId);

		if (movie == null) {
			throw new IllegalArgumentException("Invalid Movie ID");
		}

		// Khôi phục suất chiếu (thay đổi isDeleted thành false)
		movie.setDeleted(true);

		// Lưu thay đổi vào cơ sở dữ liệu
		movieRepository.save(movie);

		// Thêm thông báo thành công
		redirectAttributes.addFlashAttribute("khoiphucphimMessage", "Phim đã được khôi phục!");

		// Quay lại trang danh sách suất chiếu
		return "redirect:/flim";
	}

	// API để tìm kiếm đạo diễn
	@GetMapping("/directors")
	public List<String> getDirectors(@RequestParam String query) {
		return movieService.findDirectorsByQuery(query);
	}

	// API để tìm kiếm diễn viên
	@GetMapping("/actors")
	public List<String> getActors(@RequestParam String query) {
		return movieService.findActorsByQuery(query);
	}

	// API để tìm kiếm thể loại
	@GetMapping("/genres")
	public List<String> getGenres(@RequestParam String query) {
		return movieService.findGenresByQuery(query);
	}

	// Danh sách sản phẩm
	@GetMapping("/product")
	public String ProductList(Model model,HttpSession session) {
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		model.addAttribute("products", productRepository.findAll());
		model.addAttribute("products", productRepository.findByIsDeletedTrueOrderByProductIdDesc());
		model.addAttribute("deletedProduct", productRepository.findByIsDeletedFalseOrderByProductIdDesc());
		model.addAttribute("productBean", new ProductBean());
		return "admin/components/product";
	}

	// Trang thêm sản phẩm mới
	@GetMapping("/add-product")
	public String addProduct(Model model) {
		model.addAttribute("productBean", new ProductBean());
		return "admin/components/product";
	}

	@PostMapping("/add-product")
	public String addProductsave(@Valid @ModelAttribute("productBean") ProductBean productBean, BindingResult error,
			RedirectAttributes redirectAttributes, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("products", productRepository.findAll());
			model.addAttribute("errorMessage", "Vui lòng kiểm tra các trường đã điền.");
			return "admin/components/product";
		}
		ProductEntity productEntity = new ProductEntity();
		productEntity.setProductName(productBean.getProductName());
		productEntity.setProductDescription(productBean.getProductDescription());
		productEntity.setProductPrice(productBean.getProductPrice());
		productEntity.setProductImage(productBean.getProductImage());
		productEntity.setDeleted(true);

		redirectAttributes.addFlashAttribute("sanphamMessage", "Sản phẩm đã được thêm thành công!");
		productRepository.save(productEntity);
		return "redirect:/product"; // Đảm bảo redirect tới danh sách sản phẩm
	}

	// Trang cập nhật thông tin sản phẩm
	@GetMapping("/update-product")
	public String updateProductForm(@RequestParam("productId") Integer productId, Model model) {
		Optional<ProductEntity> optionalProduct = productRepository.findById(productId);
		if (optionalProduct.isPresent()) {
			ProductEntity productEntity = optionalProduct.get();
			ProductBean productBean = new ProductBean();
			productBean.setProductName(productEntity.getProductName());
			productBean.setProductDescription(productEntity.getProductDescription());
			productBean.setProductPrice(productEntity.getProductPrice());
			productBean.setProductImage(productEntity.getProductImage());
			productBean.setProductId(productEntity.getProductId());
			model.addAttribute("productBean", productBean);
		} else {
			model.addAttribute("productBean", new ProductBean());
		}
		model.addAttribute("products", productRepository.findAll());
		return "admin/components/product";
	}

	@PostMapping("/update-product")
	public String updateProduct(@Valid @ModelAttribute("productBean") ProductBean productBean, BindingResult error,
			RedirectAttributes redirectAttributes, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("products", productRepository.findAll());
			model.addAttribute("cnspthatbaiMessage", "Cập nhật thất bại!");
			return "admin/components/product";
		}
		Optional<ProductEntity> optionalProduct = productRepository.findById(productBean.getProductId());
		if (optionalProduct.isPresent()) {
			ProductEntity productEntity = optionalProduct.get();
			productEntity.setProductName(productBean.getProductName());
			productEntity.setProductDescription(productBean.getProductDescription());
			productEntity.setProductPrice(productBean.getProductPrice());
			productEntity.setProductImage(productBean.getProductImage());
			redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được cập nhật thành công!");
			productRepository.save(productEntity);
		}
		return "redirect:/product";
	}

	// Xóa sản phẩm
	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam("productId") Integer productId,
			RedirectAttributes redirectAttributes) {
		ProductEntity product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Showtime ID"));

		// Đánh dấu suất chiếu là đã bị xóa
		product.setDeleted(false);

		// Lưu lại trạng thái mới
		productRepository.save(product);

		redirectAttributes.addFlashAttribute("xoasanphamMessage", "Sản phẩm đã được xóa thành công!");
		return "redirect:/product"; // Đảm bảo rằng sau khi xóa, bạn luôn tải lại danh sách suất chiếu
	}

	@PostMapping("/restore-product/{productId}")
	public String restoreProduct(@PathVariable Integer productId, RedirectAttributes redirectAttributes) {
		// Tìm suất chiếu theo ID
		ProductEntity product = productRepository.findByProductId(productId);

		if (product == null) {
			throw new IllegalArgumentException("Invalid Showtime ID");
		}

		// Khôi phục suất chiếu (thay đổi isDeleted thành false)
		product.setDeleted(true);

		// Lưu thay đổi vào cơ sở dữ liệu
		productRepository.save(product);

		// Thêm thông báo thành công
		redirectAttributes.addFlashAttribute("khoiphucsanphamMessage", "Sản phẩm đã được khôi phục!");

		// Quay lại trang danh sách suất chiếu
		return "redirect:/product";
	}

	// Điều hướng đến trang sản phẩm mà không lưu dữ liệu
	@GetMapping("/reset-product")
	public String resetProduct(Model model) {
		model.addAttribute("productBean", new ProductBean());
		model.addAttribute("products", productRepository.findAll());
		return "admin/components/product";
	}

	// Hiển thị danh sách phòng chiếu
	@GetMapping("/phongchieu")
	public String showRoomList(Model model,HttpSession session) {
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		model.addAttribute("rooms", roomRepository.findAll());
		model.addAttribute("roomBean", new RoomBean());
		return "admin/components/sweetalert";
	}

	// Trang thêm phòng mới
	@GetMapping("/add-room")
	public String addRoomPage(Model model) {
		model.addAttribute("roomBean", new RoomBean());
		return "admin/components/sweetalert";
	}

	// Lưu phòng mới
	@PostMapping("/add-room")
	public String saveNewRoom(@Valid @ModelAttribute("roomBean") RoomBean roomBean, BindingResult error, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("rooms", roomRepository.findAll());
			return "admin/components/sweetalert";
		}
		RoomEntity roomEntity = new RoomEntity();
		roomEntity.setRoomName(roomBean.getRoomName());
		roomRepository.save(roomEntity);
		return "redirect:/phongchieu";
	}

	// Trang cập nhật phòng chiếu
	@GetMapping("/update-room")
	public String updateRoomPage(@RequestParam("roomId") Integer roomId, Model model) {
		Optional<RoomEntity> optionalRoom = roomRepository.findById(roomId);
		if (optionalRoom.isPresent()) {
			RoomEntity roomEntity = optionalRoom.get();
			RoomBean roomBean = new RoomBean(roomEntity.getRoomId(), roomEntity.getRoomName());
			model.addAttribute("roomBean", roomBean);
		} else {
			model.addAttribute("roomBean", new RoomBean());
		}
		model.addAttribute("rooms", roomRepository.findAll());
		return "admin/components/sweetalert";
	}

	// Lưu cập nhật phòng chiếu
	@PostMapping("/update-room")
	public String updateRoom(@Valid @ModelAttribute("roomBean") RoomBean roomBean, BindingResult error, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("rooms", roomRepository.findAll());
			return "admin/components/sweetalert";
		}
		Optional<RoomEntity> optionalRoom = roomRepository.findById(roomBean.getRoomId());
		if (optionalRoom.isPresent()) {
			RoomEntity roomEntity = optionalRoom.get();
			roomEntity.setRoomName(roomBean.getRoomName());
			roomRepository.save(roomEntity);
		}
		return "redirect:/phongchieu";
	}

	// Xóa phòng chiếu
	@PostMapping("/delete-room")
	public String deleteRoom(@RequestParam("roomId") Integer roomId) {
		roomRepository.deleteById(roomId);
		return "redirect:/phongchieu";
	}

	// Reset trang thêm hoặc cập nhật
	@GetMapping("/reset-room")
	public String resetRoom(Model model) {
		model.addAttribute("roomBean", new RoomBean());
		model.addAttribute("rooms", roomRepository.findAll());
		return "admin/components/sweetalert";
	}

	@GetMapping("/role")
	public String roleList(Model model) {
		model.addAttribute("roles", roleRepository.findAll());
		model.addAttribute("roleBean", new RoleBean());
		return "admin/tables/role";
	}

	// Trang thêm vai trò
	@GetMapping("/add-role")
	public String addRole(Model model) {
		model.addAttribute("roleBean", new RoleBean());
		return "admin/tables/role";
	}

	// Lưu vai trò mới
	@PostMapping("/add-role")
	public String saveRole(@Valid @ModelAttribute("roleBean") RoleBean roleBean, BindingResult error, Model model) {
		if (error.hasErrors()) {
			model.addAttribute("roles", roleRepository.findAll());
			return "admin/tables/role";
		}
		RoleEntity roleEntity = new RoleEntity();
		roleEntity.setRoleName(roleBean.getRoleName());
		roleRepository.save(roleEntity);
		return "redirect:/role";
	}

	// Trang cập nhật vai trò
	@GetMapping("/update-role")
	public String updateRole(@RequestParam("roleId") Integer roleId, Model model) {
		Optional<RoleEntity> optionalRole = roleRepository.findById(roleId);
		if (optionalRole.isPresent()) {
			RoleEntity roleEntity = optionalRole.get();
			RoleBean roleBean = new RoleBean(roleEntity.getRoleId(), roleEntity.getRoleName());
			model.addAttribute("roleBean", roleBean);
		} else {
			model.addAttribute("errorMessage", "Vai trò không tồn tại.");
		}
		model.addAttribute("roles", roleRepository.findAll());
		return "admin/tables/role";
	}

	// Lưu vai trò sau khi cập nhật
	@PostMapping("/update-role")
	public String saveUpdatedRole(@Valid @ModelAttribute("roleBean") RoleBean roleBean, BindingResult error,
			Model model) {
		if (error.hasErrors()) {
			model.addAttribute("roles", roleRepository.findAll());
			return "admin/tables/role";
		}
		Optional<RoleEntity> optionalRole = roleRepository.findById(roleBean.getRoleId());
		if (optionalRole.isPresent()) {
			RoleEntity roleEntity = optionalRole.get();
			roleEntity.setRoleName(roleBean.getRoleName());
			roleRepository.save(roleEntity);
		}
		return "redirect:/role";
	}

	// Xóa vai trò
	@PostMapping("/delete-role")
	public String deleteRole(@RequestParam("roleId") Integer roleId, Model model) {
		if (!roleRepository.existsById(roleId)) {
			model.addAttribute("errorMessage", "Không tìm thấy vai trò cần xóa.");
			return "admin/tables/role";
		}
		roleRepository.deleteById(roleId);
		return "redirect:/role";
	}

	// Reset trạng thái form
	@GetMapping("/reset-role")
	public String resetRole(Model model) {
		model.addAttribute("roleBean", new RoleBean());
		model.addAttribute("roles", roleRepository.findAll());
		return "admin/tables/role";
	}

	@GetMapping("/seat")
	public String getSeatsByRoom(@RequestParam(value = "roomId", defaultValue = "1", required = false) Integer roomId,
			@RequestParam(value = "seatId", required = false) Integer seatId, Model model,HttpSession session) {
		// Lấy danh sách tất cả các phòng
		List<RoomDTO> rooms = roomService.getAllRooms();
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		// Lấy danh sách ghế theo phòng
		List<SeatEntity> seats = seatService.getSeatsWithRooms();
		boolean canAddSeat = false;

		if (roomId != null) {
			seats = seatService.getSeatsByRoomId(roomId);
			int seatCount = seatService.countSeatsInRoom(roomId);
			canAddSeat = seatCount < 49; // Kiểm tra số ghế

			// Nếu seatId sđược truyền vào, lấy thông tin ghế cần sửa
			if (seatId != null) {
				SeatEntity seatToEdit = seatService.getSeatById(seatId);
				model.addAttribute("seatToEdit", seatToEdit);
			}
		}

		model.addAttribute("rooms", rooms);
		model.addAttribute("seats", seats);
		model.addAttribute("selectedRoomId", roomId);
		model.addAttribute("canAddSeat", canAddSeat);
		return "admin/components/seat"; // Trả về trang view
	}

	@PostMapping("/seat/update-price")
	public String updatePriceSeat(@RequestParam("seatId") Integer seatId, // Lấy id ghế
			@RequestParam("seatPrice") Double seatPrice, // Lấy giá ghế mới
			@RequestParam("roomId") Integer roomId, // Lấy id phòng
			@RequestParam("seatName") String seatName, // Lấy id phòng
			RedirectAttributes redirectAttributes) {

		// Kiểm tra nếu giá ghế hợp lệ
		if (seatPrice <= 0) {
			redirectAttributes.addFlashAttribute("error", "Giá ghế không hợp lệ.");
			return "redirect:/seat?roomId=" + roomId;
		}

		// Cập nhật giá ghế trong database
		boolean updated = seatService.updateSeatPrice(seatId, seatPrice); // Gọi phương thức service để cập nhật

		// Kiểm tra kết quả cập nhật
		if (updated) {
			redirectAttributes.addFlashAttribute("success", "Cập nhật giá ghế thành công ghế: " + seatName);

		} else {
			redirectAttributes.addFlashAttribute("error", "Cập nhật không thành công.");
		}

		// Trở lại trang danh sách ghế của phòng
		return "redirect:/seat?roomId=" + roomId;
	}

	@PostMapping("/seat/add")
	public String addSeat(@Valid @ModelAttribute("seat") SeatEntity seat, BindingResult result,
			@RequestParam("roomId") Integer roomId, RedirectAttributes redirectAttributes, Model model) {

		// Kiểm tra xem tên ghế đã tồn tại trong phòng chưa
		if (seatService.isSeatNameExist(roomId, seat.getSeatName())) {
			redirectAttributes.addFlashAttribute("error", "Tên ghế đã tồn tại trong phòng này.");
			return "redirect:/seat?roomId=" + roomId;
		}

		if (result.hasErrors()) {
			// Nếu có lỗi khác, hiển thị thông báo lỗi
			model.addAttribute("error", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại.");
			model.addAttribute("seat", seat);
			model.addAttribute("roomId", roomId); // Để giữ lại thông tin phòng hiện tại
			return "redirect:/seat?roomId=" + roomId; // Trả về form thêm ghế
		}

		// Kiểm tra logic nghiệp vụ: Phòng đã đủ số ghế chưa
		boolean seatAdded = seatService.addSeat(roomId, seat.getSeatName(), seat.getStatus(), seat.getSeatPrice());

		if (!seatAdded) {
			redirectAttributes.addFlashAttribute("error", "Phòng đã đủ số lượng ghế.");
		} else {
			redirectAttributes.addFlashAttribute("success", "Ghế đã được thêm thành công.");
		}

		return "redirect:/seat?roomId=" + roomId; // Quay lại trang danh sách ghế
	}

	@GetMapping("/invoice")
	public String getInvoices(Model model,HttpSession session) {
		List<InvoiceEntity> invoices = invoiceService.getAllInvoices();
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		model.addAttribute("invoices", invoices);
		return "admin/components/hoadon";
	}

	@GetMapping("/cinemaWithRoom")
	public String cinemaWithRoom(@RequestParam(required = false) Integer cinemaId, Model model,HttpSession session) {
		// Lấy danh sách tất cả rạp
		List<CinemaInformationEntity> cinemas = cinemaService.getAllCinemas();
		model.addAttribute("cinemas", cinemas);
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		// Nếu cinemaId không được truyền, đặt mặc định là 1
		if (cinemaId == null) {
			cinemaId = 1; // Giá trị mặc định
		}

		// Lấy danh sách phòng theo cinemaId
		List<RoomEntity> rooms = roomService.getRoomsByCinemaId(cinemaId);
		model.addAttribute("rooms", rooms);

		// Để giữ trạng thái chọn dropdown
		model.addAttribute("selectedCinemaId", cinemaId);

		return "admin/components/cinemaWithRoom";
	}

	@GetMapping("/seatWithRoomAndCinema")
	public String seatWithRoomAndCinema(@RequestParam(required = false) Integer cinemaId,
			@RequestParam(required = false) Integer roomId, Model model,HttpSession session) {
		// Lấy danh sách tất cả rạp
		
		
		 AccountEntity acc = (AccountEntity) session.getAttribute("acc");
	        if (acc == null) {
	            return "main/user/user-login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
	        } 
	// Kiểm tra nếu người dùng là admin
	        if (!acc.getRole().getRoleName().equalsIgnoreCase("admin")) {
	            return "redirect:/printError"; // Trả về trang 404 nếu không phải admin
	        }
		List<CinemaInformationEntity> cinemasAll = cinemaService.getAllCinemas();
		model.addAttribute("cinemas", cinemasAll);

		// Lấy danh sách phòng theo cinemaId
		List<RoomEntity> rooms = null;
		if (cinemaId != null) {
			rooms = roomService.getRoomsByCinemaId(cinemaId);
		}
		model.addAttribute("rooms", rooms);
		model.addAttribute("selectedCinemaId", cinemaId); // Để giữ trạng thái chọn rạp
		model.addAttribute("selectedRoomId", roomId); // Để giữ trạng thái chọn phòng

		// Lấy danh sách ghế theo roomId
		List<SeatEntity> seats = null;
		if (roomId != null) {
			seats = seatService.getSeatsByRoomId(roomId);
		}
		model.addAttribute("seats", seats);

		return "admin/components/seatWithRoomAndCinema"; // Trả về trang view
	}

	@PostMapping("/updateSeatPrice")
	public String updateSeatPriceWithRoom(@RequestParam("cinemaId") int cinemaId, @RequestParam("roomId") int roomId,
			@RequestParam("normalPrice") Double normalPrice, @RequestParam("vipPrice") Double vipPrice,
			RedirectAttributes redirectAttributes) {

		// Kiểm tra dữ liệu đầu vào
		if (normalPrice == null || normalPrice < 0) {
			redirectAttributes.addFlashAttribute("error", "Giá thường phải là số lớn hơn 0!");
			return "redirect:/seatWithRoomAndCinema?cinemaId=" + cinemaId + "&roomId=" + roomId;
		}

		if (vipPrice == null || vipPrice < 0) {
			redirectAttributes.addFlashAttribute("error", "Giá VIP phải là số lớn hơn 0!");
			return "redirect:/seatWithRoomAndCinema?cinemaId=" + cinemaId + "&roomId=" + roomId;
		}

		// Gọi service để lấy danh sách ghế
		List<SeatEntity> seats = seatService.getSeatsByRoomId(roomId);

		if (seats.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy ghế trong phòng này!");
			return "redirect:/seatWithRoomAndCinema?cinemaId=" + cinemaId + "&roomId=" + roomId;
		}

		// Cập nhật giá
		seats.forEach(seat -> {
			if (seat.getStatus().equalsIgnoreCase("Thường")) {
				seat.setSeatPrice(normalPrice);
			} else if (seat.getStatus().equalsIgnoreCase("VIP")) {
				seat.setSeatPrice(vipPrice);
			}
		});

		// Lưu ghế cập nhật
		seatService.saveSeats(seats);

		// Gửi thông báo thành công
		redirectAttributes.addFlashAttribute("success", "Cập nhật giá vé thành công!");

		return "redirect:/seatWithRoomAndCinema?cinemaId=" + cinemaId + "&roomId=" + roomId;
	}

	// QKhanh push code 12/12/24,,, all code admin....

}