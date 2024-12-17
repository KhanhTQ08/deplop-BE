package com.datn.demo.Controllers;

import com.datn.demo.DTO.SeatUpdateDTO;
import com.datn.demo.Services.SeatService2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SeatWebSocketController {

    @Autowired
    private SeatService2 seatService2;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/seat/update")
    public void updateSeatStatus(SeatUpdateDTO seatUpdate) {
        seatService2.updateSeatStatus(seatUpdate.getSeatId(), seatUpdate.getStatus(), seatUpdate.getUpdatedBy());
        messagingTemplate.convertAndSend("/topic/seats", seatUpdate);
    }

    @MessageMapping("/seat/requestStatus")
    public void sendAllSeatStatusToClients() {
        Map<String, SeatUpdateDTO> seatStatusMap = seatService2.getAllSeatStatus();
        // Gửi trạng thái tất cả các ghế đến /topic/seatStatus
        messagingTemplate.convertAndSend("/topic/seatStatus", seatStatusMap);
    }

}
