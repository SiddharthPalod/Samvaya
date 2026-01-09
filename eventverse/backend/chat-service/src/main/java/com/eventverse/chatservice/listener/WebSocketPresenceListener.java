package com.eventverse.chatservice.listener;

import com.eventverse.chatservice.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPresenceListener.class);
    private final PresenceService presenceService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Extract roomId and userId from session attributes (set by WebSocketAuthInterceptor)
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String userId = (String) sessionAttributes.get("userId");
            String roomId = (String) sessionAttributes.get("roomId");
            
            if (userId != null && roomId != null) {
                presenceService.userOnline(roomId, userId);
                logger.info("User {} connected to room {}", userId, roomId);
            } else {
                logger.warn("WebSocket connected but missing userId or roomId in session {}", sessionId);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Extract roomId and userId from session attributes
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String userId = (String) sessionAttributes.get("userId");
            String roomId = (String) sessionAttributes.get("roomId");
            
            if (userId != null && roomId != null) {
                presenceService.userOffline(roomId, userId);
                logger.info("User {} disconnected from room {}", userId, roomId);
            } else {
                logger.warn("WebSocket disconnected but missing userId or roomId in session {}", sessionId);
            }
        }
    }
}

