package com.chatforum.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    private String groupName;
    private String description;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection
    @CollectionTable(name = "forum_group_members", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "member_username") // 👈 Changed from "member_email"
    private List<String> members = new ArrayList<>();


    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // 👈 ADD THIS
    private List<ForumPost> posts = new ArrayList<>();

    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }
}