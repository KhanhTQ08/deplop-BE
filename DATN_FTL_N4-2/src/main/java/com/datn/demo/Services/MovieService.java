package com.datn.demo.Services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;
import com.datn.demo.Repositories.MovieRepository;

@Service
public class MovieService {
	@Autowired
	private MovieRepository movieRepository;
	@Autowired
	private ShowtimeService showtimeService;
	public List<MovieEntity> getAllMovies() {
		return movieRepository.findAll();
	}


	 public List<MovieEntity> findMoviesByKeyword(String keyword) {
	        return movieRepository.findByKeywordContainingIgnoreCase(keyword);
	    }


	public Page<MovieEntity> getMoviesWithPagination(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return movieRepository.findAll(pageable);
	}

	// Lưu phim mới
	public MovieEntity saveMovie(MovieEntity movie) {
		return movieRepository.save(movie);
	}

	// Xóa phim
	public void deleteMovie(int movieId) {
		movieRepository.deleteById(movieId);
	}

	public Optional<MovieEntity> getMovieById(Integer movieId) {
		// Sử dụng phương thức findById của repository để tìm movie theo ID
		return movieRepository.findById(movieId);
	}



	public String capitalizeWords(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		String[] words = str.split(" ");
		StringBuilder capitalizedWords = new StringBuilder();
		for (String word : words) {
			if (word.length() > 0) {
				capitalizedWords.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
			}
		}
		return capitalizedWords.toString().trim();
	}
	// Tìm kiếm đạo diễn theo từ khóa
	public List<String> findDirectorsByQuery(String query) {
		return movieRepository.findDirectorsByQuery(query);
	}

	// Tìm kiếm diễn viên theo từ khóa
	public List<String> findActorsByQuery(String query) {
		return movieRepository.findActorsByQuery(query);
	}

	// Tìm kiếm thể loại theo từ khóa
	public List<String> findGenresByQuery(String query) {
		return movieRepository.findGenresByQuery(query);
	}
}
