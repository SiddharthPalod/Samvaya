package com.eventverse.chatservice.controller;

import com.eventverse.chatservice.repository.ReadReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/chat/read-receipts")
@RequiredArgsConstructor
public class ReadReceiptController {

    private final ReadReceiptRepository readReceiptRepository;

    @GetMapping("/message/{messageId}")
    public Map<String, Object> getReaders(@PathVariable String messageId) {
        Set<Object> readers = readReceiptRepository.getReaders(messageId);
        return Map.of(
                "messageId", messageId,
                "readers", readers,
                "readCount", readers.size()
        );
    }

    @GetMapping("/user/{userId}/room/{roomId}")
    public Map<String, Object> getUserReadMessages(
            @PathVariable String userId,
            @PathVariable String roomId) {
        Set<Object> readMessages = readReceiptRepository.getUserReadMessages(userId, roomId);
        return Map.of(
                "userId", userId,
                "roomId", roomId,
                "readMessages", readMessages,
                "readCount", readMessages.size()
        );
    }

    @GetMapping("/check")
    public Map<String, Boolean> hasRead(
            @RequestParam String messageId,
            @RequestParam String userId) {
        boolean hasRead = readReceiptRepository.hasRead(messageId, userId);
        return Map.of("hasRead", hasRead);
    }
}
