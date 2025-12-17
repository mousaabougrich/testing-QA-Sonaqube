package com.wallet.biochain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time updates
 * - Block mined events
 * - Transaction confirmations
 * - Network status updates
 * - Balance updates
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        config.enableSimpleBroker(
                "/topic",    // For broadcasting to all subscribers
                "/queue"     // For point-to-point messages
        );

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // In production, specify your Flutter app's origin
                .withSockJS();           // Enable SockJS fallback options

        // Register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins("*");
    }
}