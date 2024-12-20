package com.datn.demo.Controllers;

import com.datn.demo.Entities.ConnectionInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ConnectionWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ConnectionWebSocketController(@Qualifier("brokerMessagingTemplate") SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/seat/connect")
    public void handleConnect(ConnectionInfo connectionInfo) {
        System.out.println("Tab mới vừa kết nối WebSocket với clientId: " + connectionInfo.getClientId());

        // Gửi thông báo tới tất cả các client
        messagingTemplate.convertAndSend("/topic/connections", connectionInfo);
    }
}
