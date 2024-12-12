package com.datn.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.demo.Entities.SeatEntity;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Integer> {
    List<SeatEntity> findByRoomRoomId(int roomId);

    @Query("SELECT COUNT(s) FROM SeatEntity s WHERE s.room.roomId = :roomId")
    int countSeatsByRoomId(@Param("roomId") Integer roomId);

    boolean existsBySeatName(String seatName);


    @Query("SELECT s FROM SeatEntity s JOIN FETCH s.room r")
    List<SeatEntity> findSeatsWithRooms();
}
