package com.eventverse.chatservice.controller;

import com.eventverse.chatservice.model.ChatMessage;
import com.eventverse.chatservice.repository.ChatRepository;
import com.eventverse.chatservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepository;
    private final RateLimitService rateLimitService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessage message,
                                         @RequestHeader("X-User-Id") String userId,
                                         @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        // Rate limiting check (commented out by default)
        if (!rateLimitService.isAllowed(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Please slow down."));
        }

        message.setSenderId(userId);
        if (userEmail != null) {
            message.setSenderEmail(userEmail);
        }
        message.setTimestamp(System.currentTimeMillis());
        message.setId(UUID.randomUUID().toString());

        chatRepository.saveMessage(message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages")
    public List<ChatMessage> getMessages(
            @RequestParam String roomId,
            @RequestParam(defaultValue = "0") long since) {
        return chatRepository.getMessages(roomId, since);
    }

    /**
     * Get paginated messages for a room.
     * @param roomId The room/event ID
     * @param page Page number (0-indexed, default 0)
     * @param size Page size (default 50)
     * @return Paginated response with messages
     */
    @GetMapping("/messages/paginated")
    public ResponseEntity<Map<String, Object>> getMessagesPaginated(
            @RequestParam String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<ChatMessage> messages = chatRepository.getMessagesPaginated(roomId, page, size);
        long totalMessages = chatRepository.getMessageCount(roomId);
        int totalPages = (int) Math.ceil((double) totalMessages / size);

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalMessages", totalMessages);
        response.put("totalPages", totalPages);
        response.put("hasNext", page < totalPages - 1);
        response.put("hasPrevious", page > 0);

        return ResponseEntity.ok(response);
    }
}
