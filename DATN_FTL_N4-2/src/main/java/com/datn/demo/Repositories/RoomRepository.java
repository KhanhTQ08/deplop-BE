package com.datn.demo.Repositories;

import com.datn.demo.Entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Integer> {

    // Bạn có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần
    // Sửa lại để truy cập cinema.cinemaId
    List<RoomEntity> findByCinema_CinemaId(Integer cinemaId);

    @Query("SELECT DISTINCT r FROM RoomEntity r WHERE r.cinema.cinemaId = :cinemaId")
    List<RoomEntity> findRoomsByCinemaId(@Param("cinemaId") Integer cinemaId);

}
