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

    public List<CinemaInformationEntity> getAllCinemas() {
        return cinemaInformationRepository.findAll();
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

    // Xóa rạp theo ID
    public boolean deleteCinema(int id) {
        if (cinemaInformationRepository.existsById(id)) {
            cinemaInformationRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
