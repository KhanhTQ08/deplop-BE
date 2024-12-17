package com.datn.demo.Repositories;

import com.datn.demo.Entities.ProductEntity;
import com.datn.demo.Entities.ShowtimeEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
	List<ProductEntity> findByIsDeletedFalseOrderByProductIdDesc();
	ProductEntity findByProductId(int productId);
	  boolean existsByProductName(String productName);

	List<ProductEntity> findByIsDeletedTrueOrderByProductIdDesc();
	// Bạn có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần
}
