package com.chatforum.dto;

import com.chatforum.enums.Enums;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private String content;
    private String createdBy;
    private Enums.MessageType messageType;
    private List<AttachmentDto> attachments;
}
