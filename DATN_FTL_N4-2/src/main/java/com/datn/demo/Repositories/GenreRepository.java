package com.datn.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.demo.Entities.GenreEntity;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Integer> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    // Ví dụ: tìm thể loại theo tên
    GenreEntity findByGenreName(String genreName);
}
