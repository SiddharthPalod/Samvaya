package com.eventverse.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingIndicator {
    private String userId;
    private String roomId;
    private boolean isTyping;
    private long timestamp;
}
