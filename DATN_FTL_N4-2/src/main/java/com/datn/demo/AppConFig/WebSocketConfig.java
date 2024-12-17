package com.datn.demo.AppConFig;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-seat")
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:8081", "https://yourdomain.com") // Cho phép các origin cụ thể
                .withSockJS(); // Kích hoạt SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Kênh gửi tin nhắn cho client
        config.setApplicationDestinationPrefixes("/app"); // Tiền tố cho tin nhắn gửi đến server
    }
}
