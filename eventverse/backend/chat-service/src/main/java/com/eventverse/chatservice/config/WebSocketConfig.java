package com.eventverse.chatservice.config;

import com.eventverse.chatservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat.
 * 
 * Scaling options:
 * 1. Simple broker (default): Works for single instance. For multiple instances, 
 *    use sticky sessions in load balancer.
 * 2. External STOMP broker: Use RabbitMQ/ActiveMQ with STOMP support for true 
 *    multi-instance scaling (configure via enableStompBrokerRelay).
 * 3. Redis pub/sub: Can be used for message distribution across instances.
 * 
 * Note: Kafka is better suited for event streaming, not WebSocket message brokering.
 * For WebSocket scaling, prefer Redis pub/sub or an external STOMP broker.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${chat.websocket.broker:simple}")
    private String brokerType;

    private final JwtService jwtService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new WebSocketAuthInterceptor(jwtService))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        if ("stomp-relay".equalsIgnoreCase(brokerType)) {
            // Use external STOMP broker (e.g., RabbitMQ with STOMP plugin)
            // Configure broker host/port via application.properties
            // registry.enableStompBrokerRelay("/topic")
            //         .setRelayHost("localhost")
            //         .setRelayPort(61613)
            //         .setClientLogin("guest")
            //         .setClientPasscode("guest");
            throw new IllegalStateException("STOMP relay broker not yet configured. Use 'simple' broker or configure external STOMP broker.");
        } else {
            // Simple in-memory broker (works for single instance)
            // For multiple instances, use sticky sessions in load balancer
            // OR implement Redis pub/sub for message distribution
            registry.enableSimpleBroker("/topic");
        }
        registry.setApplicationDestinationPrefixes("/app");
    }
}

