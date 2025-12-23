package com.chatforum.entity;

import com.chatforum.enums.Enums;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private Enums.AttachmentType attachmentType;


    @Column(name="file_data")
    private byte[] fileData;  // → maps to bytea in PostgreSQL   // PostgreSQL compatible

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    @JsonBackReference // avoids infinite recursion when serializing
    private ForumPost post;

    // Constructor to create attachment with file data
    public Attachment(String fileName, String fileType, Long fileSize, Enums.AttachmentType attachmentType, byte[] fileData) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.attachmentType = attachmentType;
        this.fileData = fileData;
        this.createdAt = LocalDateTime.now();
    }
}
