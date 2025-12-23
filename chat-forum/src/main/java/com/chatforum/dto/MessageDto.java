package com.chatforum.dto;

import com.chatforum.dto.AttachmentDto;
import com.chatforum.enums.Enums;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String type;
    private String content;
    private String sender;
    private Long groupId;
    private Long postId;
    private LocalDateTime timestamp;
    private List<AttachmentDto> attachments;

    public void setSignal(CallSignalDto signal) {
    }

    public void setMessageType(Enums.MessageType messageType) {
    }

    public void setAction(String action) {
    }

    public void setTargetUser(String targetUser) {
    }

    public void setParticipants(ArrayList<String> strings) {

    }

    public void setPayload(Object payload) {

    }
}
