package com.eventverse.chatservice.controller;
import com.eventverse.chatservice.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @PostMapping("/online")
    public void online(@RequestParam String roomId,
                       @RequestHeader("X-User-Id") String userId) {
        presenceService.userOnline(roomId, userId);
    }

    @PostMapping("/offline")
    public void offline(@RequestParam String roomId,
                        @RequestHeader("X-User-Id") String userId) {
        presenceService.userOffline(roomId, userId);
    }

    @GetMapping
    public Set<Object> onlineUsers(@RequestParam String roomId) {
        return presenceService.getOnlineUsers(roomId);
    }
}

