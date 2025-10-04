package com.api.process_scheduling.configs;

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
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*") // Allow all origins for CORS
        .withSockJS(); // Endpoint for WebSocket connections with SockJS fallback

    registry.addEndpoint("/stomp")
        .setAllowedOriginPatterns("*"); // No .withSockJS()
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/process-scheduler"); // Prefix for outgoing messages
    config.setApplicationDestinationPrefixes("/app"); // Prefix for incoming messages
  }
}
