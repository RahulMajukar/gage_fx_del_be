// src/main/java/com/chatforum/dto/CallActionDto.java
package com.chatforum.dto;

import lombok.Data;

@Data
public class CallActionDto {
    private String action;      // "call-started", "call-ended"
    private String initiator;
    private Long groupId;
    private long timestamp;
}