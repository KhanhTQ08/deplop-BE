package com.datn.demo.Controllers;

import com.datn.demo.Beans.AccountBean;
import com.datn.demo.DTO.ShowtimeNotificationDTO;
import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Entities.RoleEntity;
import com.datn.demo.Entities.TicketEntity;
import com.datn.demo.Repositories.AccountRepository;
import com.datn.demo.Repositories.InvoiceRepository;
import com.datn.demo.Repositories.RoleRepository;
import com.datn.demo.Services.AccountService;
import com.datn.demo.Services.MovieService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
	@Autowired
	private AccountService accountService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private AccountBean accountBean;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private MovieService movieService;
	@Autowired
	private InvoiceRepository invoiceRepository;
	
	@GetMapping("/login")
	public String userLogin(Model model, HttpSession session) {
		
		return "main/user/user-login";
	}

	@GetMapping("/profile")
	public String userProfile(Model model, HttpSession session) {
		AccountEntity account = (AccountEntity) session.getAttribute("acc");
		 if (account != null && account.getRole().getRoleName().equalsIgnoreCase("admin")) {
		        return "redirect:/printError"; // Trả về trang 404 nếu là admin
		    }
		model.addAttribute("account", account);
		return "main/user/profile"; // Trả về trang profile
	}

	@PostMapping("/profile/update")
	public String updateProfile(@ModelAttribute AccountEntity accountEntity, RedirectAttributes redirectAttributes,
	                            HttpSession session, Model model) {
	    AccountEntity currentUser = (AccountEntity) session.getAttribute("acc");

	    if (currentUser != null) {
	        boolean hasError = false;

	        // Kiểm tra số điện thoại
	     // Kiểm tra số điện thoại
	        String phoneNumber = accountEntity.getPhoneNumber();
	        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
	            if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty()) {
	                redirectAttributes.addFlashAttribute("phoneError", "Số điện thoại không được để trống!");
	                hasError = true;
	            }
	        } else if (!phoneNumber.matches("0\\d{9}")) {
	            redirectAttributes.addFlashAttribute("phoneError", "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số!");
	            hasError = true;
	        }


	        // Kiểm tra tên đầy đủ
	        String fullName = accountEntity.getFullName();
	        if (fullName == null || fullName.trim().isEmpty()) {
	            redirectAttributes.addFlashAttribute("nameError", "Tên không được để trống!");
	            hasError = true;
	        } else {
	            // Biểu thức chính quy để kiểm tra ký tự đặc biệt
	            String regex = "^[a-zA-Z0-9\\s]*$";  // Chỉ cho phép chữ cái, số và khoảng trắng
	            if (!fullName.matches(regex)) {
	                redirectAttributes.addFlashAttribute("nameError", "Tên không được chứa ký tự đặc biệt!");
	                hasError = true;
	            }
	        }


	        // Nếu có lỗi, quay lại trang profile
	        if (hasError) {
	            return "redirect:/profile";
	        }

	        // Không có lỗi: cập nhật thông tin
	        currentUser.setFullName(accountEntity.getFullName());
	        currentUser.setEmail(accountEntity.getEmail());
	        currentUser.setPhoneNumber(accountEntity.getPhoneNumber());

	        // Lưu thông tin cập nhật vào database
	        accountRepository.save(currentUser);

	        // Cập nhật thông tin trong session
	        session.setAttribute("acc", currentUser);

	        redirectAttributes.addFlashAttribute("updateSuccess", "Cập nhật thông tin thành công!");
	    }

	    return "redirect:/profile"; // Quay lại trang profile
	}


	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {

		session.removeAttribute("acc"); // Xóa tài khoản
		redirectAttributes.addFlashAttribute("message", "Bạn đã đăng xuất thành công!");

		return "redirect:/login?logout=true";
	}

	@PostMapping("/login")
	public String loginCheck(@RequestParam(name = "path", defaultValue = "") String path,
			@RequestParam("username") String username, @RequestParam("password") String password,
			RedirectAttributes redirectAttributes,
			Model model, HttpServletResponse response, HttpSession session) {

		if (username.isEmpty() || password.isEmpty()) {
			model.addAttribute("empty", "Tên đăng nhập và mật khẩu không được để trống!");
			return "main/user/user-login"; // Trả về trang đăng nhập
		}

		// Kiểm tra thông tin tài khoản từ cơ sở dữ liệu
		AccountEntity acc = accountRepository.findByUsername(username);
		if (acc == null || !acc.isDeleted()) { // Kiểm tra nếu tài khoản không tồn tại hoặc bị khóa
		    model.addAttribute("errorss", "Tài khoản không tồn tại hoặc đã bị khóa!");
		    return "main/user/user-login"; // Trả về trang đăng nhập
		}
		// Kiểm tra tài khoản và mật khẩu
		if (acc == null || !passwordEncoder.matches(password, acc.getPassword())) {
			model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
			return "main/user/user-login";
		}
	
		// Tạo cookie và lưu vào session
		Cookie name = new Cookie("username", username);
		response.addCookie(name);
		session.setAttribute("acc", acc);
		name.setMaxAge(7 * 24 * 60 * 60);
		List<InvoiceEntity> invoices = invoiceRepository.findByAccount(acc); // Lấy tất cả hóa đơn của người dùng
		List<ShowtimeNotificationDTO> showtimeNotifications = new ArrayList<>();
		LocalDate currentDate = LocalDate.now(); // Lấy ngày hiện tại

		for (InvoiceEntity invoice : invoices) {
		    if (invoice.getShowtime() != null && invoice.getShowtime().getShowDate() != null) {
		        LocalDate originalDate = invoice.getShowtime().getOriginalShowDate(); // Ngày chiếu gốc
		        LocalDate currentDateInInvoice = invoice.getShowtime().getShowDate(); // Ngày chiếu hiện tại (đã dời)

		        LocalTime originalStartTime = invoice.getShowtime().getOriginalStartTime(); // Giờ bắt đầu gốc
		        LocalTime originalEndTime = invoice.getShowtime().getOriginalEndTime(); // Giờ kết thúc gốc
		        LocalTime startTime = invoice.getShowtime().getStartTime(); // Giờ bắt đầu hiện tại
		        LocalTime endTime = invoice.getShowtime().getEndTime(); // Giờ kết thúc hiện tại

		        // Thông tin phòng chiếu
		        String roomName = (invoice.getShowtime().getRoom() != null)
		                ? invoice.getShowtime().getRoom().getRoomName()
		                : "Phòng không xác định";

		        // Danh sách ghế đã đặt (ví dụ: "A1, A2, A3")
		        StringBuilder seats = new StringBuilder();
		        if (invoice.getTickets() != null && !invoice.getTickets().isEmpty()) {
		            for (int i = 0; i < invoice.getTickets().size(); i++) {
		                TicketEntity ticket = invoice.getTickets().get(i);
		                seats.append(ticket.getSeat().getSeatName()); // Thêm tên ghế
		                if (i < invoice.getTickets().size() - 1) {
		                    seats.append(", "); // Thêm dấu phẩy nếu không phải ghế cuối
		                }
		            }
		        } else {
		            seats.append("Chưa có ghế nào được đặt.");
		        }

		        // Lý do dời lịch
		        String reason = (invoice.getShowtime().getReason() != null)
		                ? invoice.getShowtime().getReason()
		                : "Không rõ lý do";

		        // Kiểm tra nếu ngày chiếu gốc và ngày chiếu hiện tại khác nhau
		        if (originalDate != null && !originalDate.isEqual(currentDateInInvoice)) {
		            String movieName = (invoice.getShowtime().getMovie() != null)
		                    ? invoice.getShowtime().getMovie().getMovieName()
		                    : "Tên phim không xác định";

		            // Kiểm tra nếu ngày chiếu mới chưa tới, sẽ hiển thị thông báo
		            if (currentDate.isBefore(currentDateInInvoice)) {
		                // Tạo đối tượng ShowtimeNotificationDTO
		                ShowtimeNotificationDTO notification = new ShowtimeNotificationDTO(
		                		 movieName,
		                		    originalDate,
		                		    originalStartTime,
		                		    originalEndTime,
		                		    currentDateInInvoice,
		                		    startTime,
		                		    endTime,
		                		    reason,          // Lý do dời lịch
		                		    seats.toString(), // Ghế đã đặt, ví dụ "A1, A2, A3"
		                		    roomName         // Phòng chiếu
		                );

		                // Thêm đối tượng vào danh sách thông báo
		                showtimeNotifications.add(notification);
		            }
		        }
		    }
		}

		// Thêm danh sách đối tượng DTO vào model
		if (!showtimeNotifications.isEmpty()) {
			redirectAttributes.addFlashAttribute("showtimeNotifications", showtimeNotifications);
		}
		// Kiểm tra vai trò của người dùng
		String roleName = acc.getRole().getRoleName().toLowerCase();
		if (roleName.equals("admin")) {
			return "redirect:/charts/movieindex";
		} else if (roleName.equals("user")) {
			model.addAttribute("loginSuccess", "Đăng nhập thành công, Chào mừng bạn đã quay lại");

		    return "redirect:/index"; // Redirect cho user với ngôn ngữ đã lưu

		}

		model.addAttribute("errors", "Tài khoản không có quyền truy cập!");

		return "main/user/user-login";
	}

	@GetMapping("/register")
	public String userRegister(Model model) {
		model.addAttribute("account", new AccountBean());
	
		return "main/user/user-dk";
	}

	@GetMapping("/oauth2/success")
	public String oauth2Success(Model model, OAuth2AuthenticationToken authentication, HttpSession session,
	        RedirectAttributes redirectAttributes) {
	    // Lấy thông tin người dùng từ OAuth2AuthenticationToken
	    OAuth2User oauth2User = authentication.getPrincipal();
	    String email = oauth2User.getAttribute("email"); // Lấy email
	    String username = email.split("@")[0]; // Lấy phần trước dấu @
	    String password = "";
	    String fullName = oauth2User.getAttribute("name"); // Lấy tên đầy đủ
	    String phoneNumber = ""; // Đặt giá trị là "google" hoặc "facebook"

	    // Kiểm tra xem tài khoản đã tồn tại trong cơ sở dữ liệu chưa
	    AccountEntity existingAccount = accountRepository.findByEmail(email);
	    AccountEntity newAccount = null;
	    
	    // Nếu tài khoản chưa tồn tại, tạo tài khoản mới
	    if (existingAccount == null) {
	        RoleEntity userRole = roleRepository.findByRoleName("user");
	        if (userRole == null) {
	            return "redirect:/login?error=Vai%20trò%20mặc%20định%20không%20tồn%20tại";
	        }

	        // Tạo mới tài khoản
	        newAccount = new AccountEntity();
	        newAccount.setUsername(username);
	        newAccount.setPassword(password);
	        newAccount.setEmail(email);
	        newAccount.setFullName(fullName);
	        newAccount.setPhoneNumber(phoneNumber); // "google" hoặc "facebook"
	        newAccount.setRole(userRole);

	        accountRepository.save(newAccount);
	        existingAccount = newAccount; // Gán tài khoản mới vừa tạo
	    }
		List<InvoiceEntity> invoices = invoiceRepository.findByAccount(existingAccount); // Lấy tất cả hóa đơn của người dùng
		List<ShowtimeNotificationDTO> showtimeNotifications = new ArrayList<>();
		LocalDate currentDate = LocalDate.now(); // Lấy ngày hiện tại

		for (InvoiceEntity invoice : invoices) {
		    if (invoice.getShowtime() != null && invoice.getShowtime().getShowDate() != null) {
		        LocalDate originalDate = invoice.getShowtime().getOriginalShowDate(); // Ngày chiếu gốc
		        LocalDate currentDateInInvoice = invoice.getShowtime().getShowDate(); // Ngày chiếu hiện tại (đã dời)

		        LocalTime originalStartTime = invoice.getShowtime().getOriginalStartTime(); // Giờ bắt đầu gốc
		        LocalTime originalEndTime = invoice.getShowtime().getOriginalEndTime(); // Giờ kết thúc gốc
		        LocalTime startTime = invoice.getShowtime().getStartTime(); // Giờ bắt đầu hiện tại
		        LocalTime endTime = invoice.getShowtime().getEndTime(); // Giờ kết thúc hiện tại

		        // Thông tin phòng chiếu
		        String roomName = (invoice.getShowtime().getRoom() != null)
		                ? invoice.getShowtime().getRoom().getRoomName()
		                : "Phòng không xác định";

		        // Danh sách ghế đã đặt (ví dụ: "A1, A2, A3")
		        StringBuilder seats = new StringBuilder();
		        if (invoice.getTickets() != null && !invoice.getTickets().isEmpty()) {
		            for (int i = 0; i < invoice.getTickets().size(); i++) {
		                TicketEntity ticket = invoice.getTickets().get(i);
		                seats.append(ticket.getSeat().getSeatName()); // Thêm tên ghế
		                if (i < invoice.getTickets().size() - 1) {
		                    seats.append(", "); // Thêm dấu phẩy nếu không phải ghế cuối
		                }
		            }
		        } else {
		            seats.append("Chưa có ghế nào được đặt.");
		        }

		        // Lý do dời lịch
		        String reason = (invoice.getShowtime().getReason() != null)
		                ? invoice.getShowtime().getReason()
		                : "Không rõ lý do";

		        // Kiểm tra nếu ngày chiếu gốc và ngày chiếu hiện tại khác nhau
		        if (originalDate != null && !originalDate.isEqual(currentDateInInvoice)) {
		            String movieName = (invoice.getShowtime().getMovie() != null)
		                    ? invoice.getShowtime().getMovie().getMovieName()
		                    : "Tên phim không xác định";

		            // Kiểm tra nếu ngày chiếu mới chưa tới, sẽ hiển thị thông báo
		            if (currentDate.isBefore(currentDateInInvoice)) {
		                // Tạo đối tượng ShowtimeNotificationDTO
		                ShowtimeNotificationDTO notification = new ShowtimeNotificationDTO(
		                		 movieName,
		                		    originalDate,
		                		    originalStartTime,
		                		    originalEndTime,
		                		    currentDateInInvoice,
		                		    startTime,
		                		    endTime,
		                		    reason,          // Lý do dời lịch
		                		    seats.toString(), // Ghế đã đặt, ví dụ "A1, A2, A3"
		                		    roomName         // Phòng chiếu
		                );

		                // Thêm đối tượng vào danh sách thông báo
		                showtimeNotifications.add(notification);
		            }
		        }
		    }
		}

		// Thêm danh sách đối tượng DTO vào model
		if (!showtimeNotifications.isEmpty()) {
		    model.addAttribute("showtimeNotifications", showtimeNotifications);
		}
	
	    session.setAttribute("acc", existingAccount); // Đảm bảo gán tài khoản vào session
	    redirectAttributes.addFlashAttribute("showtimeNotifications", showtimeNotifications);

	    return "redirect:/index"; // Redirect cho user với ngôn ngữ đã lưu
	}

	@PostMapping("/register")
	public String saveRegister(@ModelAttribute("account") AccountBean accountBean, BindingResult result,
	        @RequestParam("username") String username, @RequestParam("password") String password,
	        @RequestParam("confirm_password") String confirmPassword, @RequestParam("fullName") String fullName,
	        @RequestParam("phoneNumber") String phoneNumber, @RequestParam("email") String email, Model model,
	        RedirectAttributes redirectAttributes) {

	    // Kiểm tra nếu bất kỳ trường nào bị bỏ trống
	    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()
	            || phoneNumber.isEmpty() || email.isEmpty()) {
	        model.addAttribute("empty", "Tất cả các trường phải được điền đầy đủ!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra độ dài username
	    if (username.length() < 6 || username.length() > 20) {
	        model.addAttribute("usernameError", "Tên đăng nhập phải có từ 6 đến 20 ký tự!");
	        return "main/user/user-dk";
	    }
	    String usernameRegex = "^[\\p{L}0-9]+$";  // Cho phép chữ cái (có dấu), số và không cho ký tự đặc biệt
	    if (!username.matches(usernameRegex)) {
	        model.addAttribute("chichochuuser", "Tên đăng nhập không được chứa ký tự đặc biệt!");
	        return "main/user/user-dk";
	    }
	    String fullNameRegex = "^[\\p{L}]+(\\s[\\p{L}]+)*$";  // Cho phép chữ cái và chỉ một khoảng trắng giữa các từ
	    if (!fullName.matches(fullNameRegex)) {
	        model.addAttribute("chichochufull", "Tên đầy đủ không được chứa ký tự đặc biệt hoặc không hợp lệ!");
	        return "main/user/user-dk";
	    }


	    // Kiểm tra nếu tên đăng nhập đã tồn tại
	    if (accountRepository.existsByUsername(username)) {
	        model.addAttribute("usernameExists", "Tên đăng nhập đã tồn tại!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra nếu email đã tồn tại
	    if (accountRepository.existsByEmail(email)) {
	        model.addAttribute("emailExists", "Email đã được sử dụng!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra độ dài mật khẩu
	    if (password.length() < 6) {
	        model.addAttribute("passwordError", "Mật khẩu phải có ít nhất 6 ký tự!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra mật khẩu xác nhận
	    if (!confirmPassword.equals(password)) {
	        model.addAttribute("confirmError", "Mật khẩu xác nhận phải giống với mật khẩu!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra định dạng email
	    String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
	    if (!email.matches(emailRegex)) {
	        model.addAttribute("emailError", "Email không đúng định dạng!");
	        return "main/user/user-dk";
	    }

	    // Kiểm tra định dạng số điện thoại
	    String phoneRegex = "^0\\d{9}$";
	    if (!phoneNumber.matches(phoneRegex)) {
	        model.addAttribute("phoneError", "Số điện thoại phải bắt đầu bằng số 0 và bao gồm đúng 10 chữ số!");
	        return "main/user/user-dk"; // Trả về trang đăng ký
	    }


	    // Tạo đối tượng AccountEntity và mã hóa mật khẩu
	    AccountEntity accountEntity = new AccountEntity();
	    accountEntity.setUsername(username);
	    accountEntity.setPassword(passwordEncoder.encode(password));
	    accountEntity.setFullName(fullName);
	    accountEntity.setPhoneNumber(phoneNumber);
	    accountEntity.setEmail(email);

	    // Gán vai trò mặc định là 'user'
	    RoleEntity userRole = roleRepository.findByRoleName("user");
	    if (userRole == null) {
	        model.addAttribute("roleError", "Vai trò mặc định không tồn tại!");
	        return "main/user/user-dk";
	    }
	    accountEntity.setRole(userRole);

	    accountRepository.save(accountEntity);
	    model.addAttribute("registerSuccess", "Đăng ký thành công!");
	    return "main/user/user-dk"; // Quay lại trang đăng ký, nhưng hiển thị thông báo thành công
	}
	@GetMapping("/forgot-password")
	public String userForgotPassword(Model model) {
		return "main/user/user-qmk";
	}
	@PostMapping("/forgot-password")
	@ResponseBody
	public ResponseEntity<Map<String, String>> sendOtp(@RequestParam("email") String email, HttpSession session) {
	    Map<String, String> response = new HashMap<>();

	    // Kiểm tra email không được để trống
	    if (email == null || email.trim().isEmpty()) {
	        response.put("error", "Email không được để trống.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    // Kiểm tra định dạng email
	    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	    if (!Pattern.matches(emailRegex, email)) {
	        response.put("error", "Email không đúng định dạng.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    // Gửi OTP qua service
	    boolean otpSent = accountService.sendOtpToEmail(email, session);

	    if (!otpSent) {
	        // Kiểm tra lý do gửi thất bại
	        LocalDateTime lastOtpTime = (LocalDateTime) session.getAttribute("lastOtpTime");
	        if (lastOtpTime != null && lastOtpTime.plusMinutes(1).isAfter(LocalDateTime.now())) {
	            response.put("error", "Bạn phải đợi ít nhất 1 phút để gửi lại OTP.");
	        } else {
	            response.put("error", "Email không tồn tại trong hệ thống.");
	        }
	        return ResponseEntity.badRequest().body(response);
	    }

	    response.put("message", "Mã OTP đã được gửi vào email của bạn.");
	    return ResponseEntity.ok(response);  // Trả về thông báo thành công
	}


    // Xử lý xác thực OTP và thay đổi mật khẩu
	@PostMapping("/verify-otp")
	@ResponseBody
	public ResponseEntity<Map<String, String>> verifyOtp(@RequestParam("otp") String otp, HttpSession session) {
	    Map<String, String> response = new HashMap<>();

	    // Kiểm tra mã OTP không được để trống
	    if (otp == null || otp.trim().isEmpty()) {
	        response.put("error", "Mã OTP không được để trống.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    // Kiểm tra OTP có đúng với session không
	    String sessionOtp = (String) session.getAttribute("otp");

	    if (sessionOtp == null || !sessionOtp.equals(otp)) {
	        response.put("error", "Mã OTP không chính xác.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    response.put("message", "Mã OTP hợp lệ. Bạn có thể thay đổi mật khẩu.");
	    return ResponseEntity.ok(response);  // Trả về thông báo thành công
	}
	@PostMapping("/reset-password")
	@ResponseBody
	public ResponseEntity<Map<String, String>> resetPassword(@RequestParam("newPassword") String newPassword,
	                                                           @RequestParam("confirmPassword") String confirmPassword,
	                                                           HttpSession session) {
	    Map<String, String> response = new HashMap<>();

	    // Kiểm tra mật khẩu và xác nhận mật khẩu
	    if (!newPassword.equals(confirmPassword)) {
	        response.put("error", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    // Kiểm tra độ dài mật khẩu
	    if (newPassword.length() < 6) {
	        response.put("error", "Mật khẩu phải có ít nhất 6 ký tự.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    // Thực hiện thay đổi mật khẩu
	    boolean isPasswordChanged = accountService.changePassword(newPassword, session);
	    if (isPasswordChanged) {
	        response.put("message", "Mật khẩu đã được thay đổi thành công.");
	        return ResponseEntity.ok(response);  // Trả về thông báo thành công
	    }

	    response.put("error", "Có lỗi xảy ra khi thay đổi mật khẩu.");
	    return ResponseEntity.badRequest().body(response);
	}


}
