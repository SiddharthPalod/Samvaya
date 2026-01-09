package com.eventverse.chatservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(String roomId) {
        return "presence:room:" + roomId;
    }

    public void userOnline(String roomId, String userId) {
        redisTemplate.opsForSet().add(key(roomId), userId);
    }

    public void userOffline(String roomId, String userId) {
        redisTemplate.opsForSet().remove(key(roomId), userId);
    }

    public Set<Object> getOnlineUsers(String roomId) {
        Set<Object> members = redisTemplate.opsForSet().members(key(roomId));
        return members != null ? members : Set.of();
    }
}
