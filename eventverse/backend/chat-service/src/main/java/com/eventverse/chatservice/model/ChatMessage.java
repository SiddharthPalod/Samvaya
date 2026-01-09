package com.eventverse.chatservice.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String roomId;   // eventId
    private String senderId;
    private String senderEmail; // User email for display
    private String content;
    private long timestamp;
}
