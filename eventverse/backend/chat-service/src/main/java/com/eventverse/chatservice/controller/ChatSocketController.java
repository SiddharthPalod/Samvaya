package com.eventverse.chatservice.controller;

import com.eventverse.chatservice.model.ChatMessage;
import com.eventverse.chatservice.model.ReadReceipt;
import com.eventverse.chatservice.model.TypingIndicator;
import com.eventverse.chatservice.repository.ChatRepository;
import com.eventverse.chatservice.repository.ReadReceiptRepository;
import com.eventverse.chatservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final ReadReceiptRepository readReceiptRepository;
    private final RateLimitService rateLimitService;

    @MessageMapping("/chat.send")
    public void send(ChatMessage message, StompHeaderAccessor headerAccessor) {
        // Extract userId and email from session attributes (set during handshake)
        String userId = null;
        String userEmail = null;
        String roomId = null;
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
            userEmail = (String) headerAccessor.getSessionAttributes().get("userEmail");
            roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        }
        
        // Rate limiting check
        if (userId != null && !rateLimitService.isAllowed(userId)) {
            // Send error back to sender
            Map<String, String> error = Map.of("error", "Rate limit exceeded. Please slow down.");
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/queue/errors",
                    error
            );
            return;
        }
        
        // Set message metadata
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());
        if (userId != null) {
            message.setSenderId(userId);
        }
        if (userEmail != null) {
            message.setSenderEmail(userEmail);
        }
        if (roomId != null && message.getRoomId() == null) {
            message.setRoomId(roomId);
        }

        // Save to Redis
        chatRepository.saveMessage(message);

        // Broadcast to all subscribers of this room
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(TypingIndicator indicator, StompHeaderAccessor headerAccessor) {
        // Extract userId from session
        String userId = null;
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }
        
        if (userId != null) {
            indicator.setUserId(userId);
            indicator.setTimestamp(System.currentTimeMillis());
            
            // Broadcast typing indicator to room (except sender)
            messagingTemplate.convertAndSend(
                    "/topic/typing/" + indicator.getRoomId(),
                    indicator
            );
        }
    }

    @MessageMapping("/chat.read")
    public void markAsRead(ReadReceipt receipt, StompHeaderAccessor headerAccessor) {
        // Extract userId from session
        String userId = null;
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }
        
        if (userId != null && receipt.getMessageId() != null) {
            receipt.setUserId(userId);
            receipt.setReadAt(System.currentTimeMillis());
            
            // Save read receipt
            readReceiptRepository.markAsRead(
                    receipt.getMessageId(),
                    receipt.getUserId(),
                    receipt.getRoomId()
            );
            
            // Broadcast read receipt update to room
            messagingTemplate.convertAndSend(
                    "/topic/read/" + receipt.getRoomId(),
                    receipt
            );
        }
    }
}

