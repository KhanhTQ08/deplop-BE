package com.datn.demo.Controllers;

import com.datn.demo.Model.SeatUpdate;
import com.datn.demo.Services.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

//    private final SeatService seatService;
//
//    public SeatController(SeatService seatService) {
//        this.seatService = seatService;
//    }
//
//    @GetMapping("/status")
//    public ResponseEntity<List<SeatUpdate>> getAllSeatStatus() {
//        List<SeatUpdate> seatStatus = seatService.getAllSeatStatus();
//        if (seatStatus == null || seatStatus.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
//        }
//        return ResponseEntity.ok(seatStatus);
//    }


}
