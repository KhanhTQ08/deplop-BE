package com.datn.demo.Services;

import com.datn.demo.Entities.CinemaInformationEntity;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;
import com.datn.demo.Repositories.CinemaInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class CinemaInformationService {

    private final CinemaInformationRepository cinemaInformationRepository;

    @Autowired
    public CinemaInformationService(CinemaInformationRepository cinemaInformationRepository) {
        this.cinemaInformationRepository = cinemaInformationRepository;
    }

//    public List<CinemaInformationEntity> getAllCinemas() {
//        return cinemaInformationRepository.findAll();
//    }
//    
    public List<CinemaInformationEntity> getAllCinemas() {
        // Chỉ lấy các bản ghi có status = true
        return cinemaInformationRepository.findByStatusTrue();
    }

    // Phương thức để lấy rạp chiếu với các suất chiếu chỉ thuộc phim được chọn
    public List<CinemaInformationEntity> getCinemasWithShowtimesByMovie(int movieId) {
        List<CinemaInformationEntity> cinemas = cinemaInformationRepository.findAll();

        for (CinemaInformationEntity cinema : cinemas) {
            // Lọc các suất chiếu theo movieId
            List<ShowtimeEntity> filteredShowtimes = cinema.getShowtimes().stream()
                    .filter(showtime -> showtime.getMovie().getMovieId() == movieId)
                    .collect(Collectors.toList());

            // Gán lại danh sách suất chiếu đã được lọc
            cinema.setShowtimes(filteredShowtimes);
        }

        return cinemas;
    }


    // Phương thức để lấy một rạp theo suất chiếu
    public CinemaInformationEntity getCinemaByShowtimeId(Integer showtimeId) {
        // Tìm rạp theo showtimeId
        return cinemaInformationRepository.findCinemaByShowtimeId(showtimeId);
    }
 // Tìm kiếm rạp theo ID
    public Optional<CinemaInformationEntity> getCinemaById(int id) {
        return cinemaInformationRepository.findById(id);
    }
    public CinemaInformationEntity getCinemaByIds(int cinemaId) {
        return cinemaInformationRepository.findById(cinemaId).orElse(null);
    }
    // Thêm mới rạp
    public CinemaInformationEntity addCinema(CinemaInformationEntity cinema) {
        if (cinema.getStatus() == null) {
            cinema.setStatus(true); // Đặt mặc định status = true
        }
        return cinemaInformationRepository.save(cinema);
    }

    // Cập nhật thông tin rạp
    public CinemaInformationEntity updateCinema(int id, CinemaInformationEntity cinemaDetails) {
        return cinemaInformationRepository.findById(id).map(cinema -> {
            cinema.setCinemaName(cinemaDetails.getCinemaName());
            cinema.setAddress(cinemaDetails.getAddress());
            cinema.setPhoneNumber(cinemaDetails.getPhoneNumber());
            cinema.setEmail(cinemaDetails.getEmail());
            return cinemaInformationRepository.save(cinema);
        }).orElseGet(() -> {
            cinemaDetails.setCinemaId(id);
            return cinemaInformationRepository.save(cinemaDetails);
        });
    }

  //   Xóa rạp theo ID
    public boolean DELETEcinema(int id) {
        if (cinemaInformationRepository.existsById(id)) {
            cinemaInformationRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public void deleteCinema(int id) throws Exception {
        CinemaInformationEntity cinema = cinemaInformationRepository.findById(id)
                .orElseThrow(() -> new Exception("Rạp không tồn tại với ID: " + id));
        
        // Cập nhật trạng thái thành false
        cinema.setStatus(false);
        
        // Lưu lại thay đổi
        cinemaInformationRepository.save(cinema);
    }
    
    public List<CinemaInformationEntity> getTrashCinemas() {
        return cinemaInformationRepository.findByStatusFalse();
    }
  

    // Khôi phục rạp từ thùng rác
    public void restoreCinema(int id) throws Exception {
        CinemaInformationEntity cinema = cinemaInformationRepository.findById(id)
                .orElseThrow(() -> new Exception("Rạp không tồn tại với ID: " + id));
        cinema.setStatus(true); // Đặt lại trạng thái là true
        cinemaInformationRepository.save(cinema); // Lưu lại thay đổi
    }
    

}
