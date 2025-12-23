package com.chatforum.dto;

import com.chatforum.entity.Attachment;
import com.chatforum.enums.Enums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Enums.AttachmentType attachmentType;
    private byte[] fileData;

    public AttachmentDto(Attachment att) {
        this.id = att.getId();
        this.fileName = att.getFileName();
        this.fileType = att.getFileType();
        this.fileSize = att.getFileSize();
        this.attachmentType = att.getAttachmentType();
        this.fileData = att.getFileData();
    }
}
