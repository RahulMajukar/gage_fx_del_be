package com.chatforum.entity;

import com.chatforum.enums.Enums;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Enums.MessageType messageType = Enums.MessageType.TEXT;

    // In ForumPost.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonBackReference // ← ADD THIS
    private ForumGroup group;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // tells Jackson this is parent in relation
    private List<Attachment> attachments = new ArrayList<>();

    public void addAttachment(Attachment attachment){
        attachments.add(attachment);
        attachment.setPost(this);
    }
}
