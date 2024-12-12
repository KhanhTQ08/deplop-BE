package com.datn.demo.Services;

import com.datn.demo.DTO.FullMovieShowtimeDTO;
import com.datn.demo.DTO.ShowtimeDetailsDTO;
import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.CinemaInformationEntity;
import com.datn.demo.Entities.InvoiceEntity;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.RoomEntity;
import com.datn.demo.Entities.ShowtimeEntity;
import com.datn.demo.Entities.TicketEntity;
import com.datn.demo.Repositories.CinemaInformationRepository;
import com.datn.demo.Repositories.InvoiceRepository;
import com.datn.demo.Repositories.MovieRepository;
import com.datn.demo.Repositories.RoomRepository;
import com.datn.demo.Repositories.ShowtimeRepository;
import com.datn.demo.Repositories.TicketRepository;
import com.datn.demo.Utility.Email;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

	@Autowired
	private ShowtimeRepository showtimeRepository;
	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private Email emailService; // Để gửi email
	@Autowired
	private InvoiceRepository invoiceRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private CinemaInformationRepository cinemaInformationRepository;
	 @Autowired
	    private MovieRepository movieRepository;

	    @Autowired
	    private RoomRepository roomRepository;
	   
	// Phương thức lấy tất cả các rạp chiếu phim
	public List<CinemaInformationEntity> getAllCinemas() {
		return cinemaInformationRepository.findAll();
	}
	 public LocalDate getBookingStartDate(int cinemaId) {
	        Optional<ShowtimeEntity> showtimeEntity = showtimeRepository.findFirstByCinemaInformation_CinemaIdOrderByShowDateAsc(cinemaId);
	        return showtimeEntity.map(ShowtimeEntity::getBookingStartDate).orElse(LocalDate.now());
	    }
	public List<ShowtimeEntity> getShowtimesByCinema(int cinemaId) {
		return showtimeRepository.findByCinemaInformation_CinemaId(cinemaId);
	}

	public List<ShowtimeEntity> getShowtimesByCinemaAndDate(int cinemaId, LocalDate date) {
		return showtimeRepository.findByCinemaInformation_CinemaIdAndShowDate(cinemaId, date);
	}

	/**
	 * Lấy tất cả các ca chiếu với thông tin chi tiết.
	 *
	 * @return Danh sách ShowtimeDetailsDTO
	 */
	public List<ShowtimeDetailsDTO> getAllShowtime() {
		return showtimeRepository.findAllShowtimeDetails();
	}

	public List<ShowtimeEntity> getShowtimesByMovieId(int movieId) {
		return showtimeRepository.findByMovie_MovieId(movieId);
	}

	public List<ShowtimeEntity> getAllShowtimes() {
		return showtimeRepository.findAll(); // Trả về tất cả các ca chiếu
	}

	public List<ShowtimeEntity> findShowtimesByCinemaAndDate(int cinemaId, LocalDate date) {
		return showtimeRepository.findShowtimesByCinemaAndDate(cinemaId, date);
	}

	/**
	 * Lấy các ca chiếu theo movieId, sắp xếp theo ngày chiếu.
	 *
	 * @param movieId ID của phim
	 * @return Danh sách ShowtimeEntity
	 */
	public List<ShowtimeEntity> getShowtimesByMovieId(Integer movieId) {
		return showtimeRepository.findByMovieMovieIdOrderByShowDate(movieId);
	}

	/**
	 * Lấy thông tin ca chiếu theo showtimeId.
	 *
	 * @param showtimeId ID của ca chiếu
	 * @return Optional chứa ShowtimeEntity nếu tìm thấy
	 */
	public Optional<ShowtimeEntity> getShowtimeById(int showtimeId) {
		return showtimeRepository.findById(showtimeId);
	}

	public void saveShowtime(ShowtimeEntity showtime) {
		showtimeRepository.save(showtime);
	}

	public void deleteShowtime(int id) {
		showtimeRepository.deleteById(id);
	}

	public void rescheduleShowtime(int showtimeId, LocalDate newShowDate, LocalTime newStartTime, LocalTime newEndTime,
			String reason) {
		Optional<ShowtimeEntity> optionalShowtime = showtimeRepository.findById(showtimeId);

		if (optionalShowtime.isPresent()) {
			ShowtimeEntity showtime = optionalShowtime.get();

			// Lưu lý do dời lịch chiếu vào entity (nếu cần thiết)
			showtime.setReason(reason); // Gắn lý do vào suất chiếu

			if (showtime.getOriginalShowDate() == null) {
				showtime.setOriginalShowDate(showtime.getShowDate());
				showtime.setOriginalStartTime(showtime.getStartTime());
				showtime.setOriginalEndTime(showtime.getEndTime());
			}

			showtime.setShowDate(newShowDate);
			showtime.setStartTime(newStartTime);
			showtime.setEndTime(newEndTime);

			showtimeRepository.save(showtime);

			// Thông báo tới khách hàng về sự thay đổi
			notifyCustomersForOldShowtime(showtime);
		} else {
			throw new RuntimeException("Không tìm thấy suất chiếu với ID: " + showtimeId);
		}
	}

	public void notifyCustomersForOldShowtime(ShowtimeEntity showtime) {
		// Truyền vào tham số LocalDate và LocalTime trực tiếp
		LocalDate originalShowDate = showtime.getOriginalShowDate();
		LocalTime originalStartTime = showtime.getOriginalStartTime();
		LocalTime originalEndTime = showtime.getOriginalEndTime();

		// Không cần kiểm tra phòng chiếu cũ nữa, bỏ qua phần phòng chiếu
		List<InvoiceEntity> invoices = invoiceRepository.findByOriginalShowtimeDetails(originalShowDate,
				originalStartTime, originalEndTime);

		// Duyệt qua các hóa đơn và gửi email thông báo
		for (InvoiceEntity invoice : invoices) {
			String emailContent = buildShowtimeChangeEmailContent(showtime);
			emailService.sendEmail(invoice.getAccount().getEmail(), "Thông báo dời lịch chiếu", emailContent);
		}
	}

	private String buildShowtimeChangeEmailContent(ShowtimeEntity showtime) {
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append(
				"<div style='font-family: sans-serif; min-width: 100%; min-height: 100vh; background-color: rgba(250, 90, 90, 0.8); padding: 2rem;'>")
				.append("<div style='max-width: 600px; margin: auto;'>")
				.append("<div style='background-color: white; padding: 2rem; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);'>")
				.append("<div style='text-align: center; border-bottom: 1px solid #eaeaea; margin-bottom: 2rem;'>")
				.append("<img src='https://drive.google.com/uc?id=1nFnGPdWnlFK1XRuZtB-8hJWFEVMp0TSF' alt='Mô tả hình ảnh' style='width: 80px; height: 80px; margin-right: 550px;' >")
				.append("<h1 style='font-size: 2rem;'>Thông Báo Dời Lịch Chiếu</h1>").append("</div>")
				.append("<p>Xin chào quý khách,</p>").append("<p>Chúng tôi xin thông báo rằng suất chiếu phim <b>")
				.append(showtime.getMovie().getMovieName()).append("</b> vào ngày <b>")
				.append(showtime.getOriginalShowDate()).append("</b> đã được dời sang ngày <b>")
				.append(showtime.getShowDate()).append("</b>.</p>").append("<p>Thời gian chiếu mới: <b>")
				.append(showtime.getStartTime()).append(" - ").append(showtime.getEndTime()).append("</b></p>")
				.append("<p>Lý do dời lịch: <b>").append(showtime.getReason()).append("</b></p>")
				.append("<p>Rạp chiếu: <b>").append(showtime.getCinemaInformation().getCinemaName()).append("</b></p>")
				.append("<p>Chúng tôi xin lỗi vì sự bất tiện này và cảm ơn quý khách đã thông cảm.</p>")
				.append("<p>Thông tin ghế ngồi đã đặt:</p>");

		// Danh sách ghế ngồi
		List<TicketEntity> tickets = ticketRepository.findTicketsByShowtimeId(showtime.getShowtimeId());
		StringBuilder seats = new StringBuilder();
		seats.setLength(0);
		for (TicketEntity ticket : tickets) {
			if (ticket.getSeat().getRoom().getRoomId() == showtime.getRoom().getRoomId()) {
				if (seats.length() > 0)
					seats.append(", ");
				seats.append(ticket.getSeat().getSeatName());
			}
		}
		htmlContent.append("<ul>");
		htmlContent.append("<li><b>Phòng chiếu - ").append(showtime.getRoom().getRoomName()).append("</b>: ")
				.append(seats.toString()).append("</li>");
		htmlContent.append("</ul>");

		// Di chuyển phần cảm ơn vào bên trong form
		htmlContent.append(
				"<p style='margin-top: 1rem;text-align: center;'>Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ chúng tôi.</p>")
				.append("<div style='text-align: center; color: #555;'>")
				.append("<h3>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</h3>").append("</div>").append("</div>") // Đóng
																													// khung
																													// thông
																													// báo
																													// chính
				.append("</div>") // Đóng khung giới hạn
				.append("</div>"); // Đóng nền ngoài
		return htmlContent.toString();
	}

	public List<ShowtimeEntity> getShowtimesForDate(LocalDate selectedDate) {
		// Giả sử bạn có một phương thức trong repository để lấy dữ liệu theo ngày
		return showtimeRepository.findByShowDate(selectedDate);
	}

	public List<ShowtimeEntity> getShowtimesByDate(LocalDate date) {
		return showtimeRepository.findByShowDate(date);
	}

	public List<ShowtimeEntity> findShowtimesByDate(LocalDate date) {
		return showtimeRepository.findByShowDate(date);
	}

	public List<FullMovieShowtimeDTO> getAllShowtimeDetails() {
		return showtimeRepository.findAllShowtimeCineMovieRoom();
	}

}
