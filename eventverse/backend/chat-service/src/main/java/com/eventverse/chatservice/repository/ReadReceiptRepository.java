package com.eventverse.chatservice.repository;

import com.eventverse.chatservice.model.ReadReceipt;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReadReceiptRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(String messageId) {
        return "read-receipt:message:" + messageId;
    }

    private String userKey(String userId, String roomId) {
        return "read-receipt:user:" + userId + ":room:" + roomId;
    }

    /**
     * Mark a message as read by a user.
     */
    public void markAsRead(String messageId, String userId, String roomId) {
        ReadReceipt receipt = new ReadReceipt(messageId, userId, roomId, System.currentTimeMillis());
        
        // Store receipt by message ID
        redisTemplate.opsForSet().add(key(messageId), userId);
        
        // Store receipt by user and room (for querying user's read messages)
        redisTemplate.opsForSet().add(userKey(userId, roomId), messageId);
    }

    /**
     * Get all users who have read a specific message.
     */
    public Set<Object> getReaders(String messageId) {
        Set<Object> readers = redisTemplate.opsForSet().members(key(messageId));
        return readers != null ? readers : Set.of();
    }

    /**
     * Check if a user has read a message.
     */
    public boolean hasRead(String messageId, String userId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(key(messageId), userId);
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * Get all message IDs that a user has read in a room.
     */
    public Set<Object> getUserReadMessages(String userId, String roomId) {
        Set<Object> messages = redisTemplate.opsForSet().members(userKey(userId, roomId));
        return messages != null ? messages : Set.of();
    }
}
