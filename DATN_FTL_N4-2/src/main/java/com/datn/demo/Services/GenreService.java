package com.datn.demo.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.demo.Entities.GenreEntity;
import com.datn.demo.Repositories.GenreRepository;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    // Tạo mới hoặc cập nhật thể loại
    public GenreEntity addGenre(GenreEntity genre) {
        // Thực hiện lưu genre vào cơ sở dữ liệu
        return genreRepository.save(genre);
    }

    public void deleteGenre(Integer genreId) {
        // Kiểm tra xem genre có tồn tại không trước khi xóa
        if (genreRepository.existsById(genreId)) {
            genreRepository.deleteById(genreId);
        }
    }
    
    // Cập nhật hoặc thêm mới genre
    public GenreEntity saveGenre(GenreEntity genre) {
        return genreRepository.save(genre);
    }
    // Lấy tất cả thể loại
    public List<GenreEntity> getAllGenres() {
        return genreRepository.findAll();
    }

    // Lấy thể loại theo ID
    public Optional<GenreEntity> getGenreById(int genreId) {
        return genreRepository.findById(genreId);
    }

    // Xóa thể loại theo ID
    public void deleteGenre(int genreId) {
        genreRepository.deleteById(genreId);
    }

    // Tìm thể loại theo tên
    public GenreEntity getGenreByName(String genreName) {
        return genreRepository.findByGenreName(genreName);
    }
    
  
}