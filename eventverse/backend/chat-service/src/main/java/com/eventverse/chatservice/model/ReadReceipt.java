package com.eventverse.chatservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceipt {
    private String messageId;
    private String userId;
    private String roomId;
    private long readAt;
}
