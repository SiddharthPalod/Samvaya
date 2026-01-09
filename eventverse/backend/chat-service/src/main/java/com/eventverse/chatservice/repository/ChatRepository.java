package com.eventverse.chatservice.repository;

import com.eventverse.chatservice.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(String roomId) {
        return "chat:room:" + roomId;
    }

    public void saveMessage(ChatMessage message) {
        redisTemplate.opsForList().rightPush(
                key(message.getRoomId()), message
        );
    }

    public List<ChatMessage> getMessages(String roomId, long since) {
        List<Object> messages =
                redisTemplate.opsForList().range(key(roomId), 0, -1);
        if (messages == null) return List.of();
        return messages.stream()
                .map(ChatMessage.class::cast)
                .filter(m -> m.getTimestamp() > since)
                .toList();
    }

    /**
     * Get paginated messages for a room.
     * @param roomId The room/event ID
     * @param page Page number (0-indexed, page 0 = oldest messages)
     * @param size Page size
     * @return List of messages in chronological order (oldest first)
     */
    public List<ChatMessage> getMessagesPaginated(String roomId, int page, int size) {
        long totalMessages = redisTemplate.opsForList().size(key(roomId));
        if (totalMessages == 0) return List.of();

        // Calculate range (Redis lists are 0-indexed, oldest at start, newest at end)
        // Page 0 = first (oldest) messages, page 1 = next batch, etc.
        long start = page * size;
        long end = Math.min(start + size - 1, totalMessages - 1);

        if (start >= totalMessages) return List.of();

        List<Object> messages = redisTemplate.opsForList().range(key(roomId), start, end);
        if (messages == null) return List.of();

        // Return in chronological order (oldest first) - no reversal needed
        return messages.stream()
                .map(ChatMessage.class::cast)
                .toList();
    }

    /**
     * Get total message count for a room.
     */
    public long getMessageCount(String roomId) {
        Long count = redisTemplate.opsForList().size(key(roomId));
        return count != null ? count : 0;
    }
}

