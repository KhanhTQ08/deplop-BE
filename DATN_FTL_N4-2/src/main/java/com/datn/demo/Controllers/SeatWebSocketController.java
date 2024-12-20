package com.datn.demo.Controllers;

import com.datn.demo.Model.SeatUpdate;
import com.datn.demo.Services.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
public class SeatWebSocketController {
    private final SeatService seatService;
    private final Map<String, SeatUpdate> seatStatusMap = new ConcurrentHashMap<>();

    //===============================================================
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    // Endpoint WebSocket để nhận yêu cầu khóa ghế
    //================================================================

    public SeatWebSocketController(SeatService seatService) {
        this.seatService = seatService;
    }


    @MessageMapping("/seat/select") // Khi client gửi tin nhắn tới /app/seat/select
    @SendTo("/topic/seats") // Gửi tin nhắn tới tất cả các client subscribe tại /topic/seats
    public SeatUpdate handleSeatSelection(SeatUpdate seatUpdate) {
        // Cập nhật trạng thái ghế vào map
        seatStatusMap.put(seatUpdate.getSeatId(), seatUpdate);
        System.out.println("Cập nhật trạng thái ghế: " + seatUpdate);
        return seatUpdate; // Trả về trạng thái ghế đã chọn
    }

    @MessageMapping("/seat/status")
    @SendTo("/topic/seats")
    public List<SeatUpdate> getAllSeatStatus() {
        // Trả về danh sách tất cả các ghế đã được cập nhật
        return new ArrayList<>(seatStatusMap.values());
    }

}
