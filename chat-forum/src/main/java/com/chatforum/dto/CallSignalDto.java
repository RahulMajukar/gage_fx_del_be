// src/main/java/com/chatforum/dto/CallSignalDto.java
package com.chatforum.dto;

import lombok.Data;

@Data
public class CallSignalDto {
    private String type;        // "offer", "answer", "ice-candidate"
    private String sender;      // username of sender
    private Long groupId;
    private String targetUser;  // Optional: if you want selective 1:1 within group
    private Object data;        // SDP (for offer/answer) or RTCIceCandidate (for ice-candidate)
}