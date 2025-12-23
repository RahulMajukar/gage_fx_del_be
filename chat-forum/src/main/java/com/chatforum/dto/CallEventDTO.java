package com.chatforum.dto;

import java.util.Map;

public class CallEventDTO {
    private String type;
    private String sender;
    private String groupId;
    private String timestamp;
    private Map<String, Object> additionalData;

    // Constructors
    public CallEventDTO() {}

    public CallEventDTO(String type, String sender, String groupId, String timestamp) {
        this.type = type;
        this.sender = sender;
        this.groupId = groupId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
}