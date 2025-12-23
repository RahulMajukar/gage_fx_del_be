// src/main/java/com/chatforum/service/ForumService.java
package com.chatforum.service;

import com.chatforum.dto.AttachmentDto;
import com.chatforum.dto.CreatePostRequest;
import com.chatforum.dto.ForumPostDto;
import com.chatforum.entity.Attachment;
import com.chatforum.entity.ForumGroup;
import com.chatforum.entity.ForumPost;
import com.chatforum.enums.Enums;
import com.chatforum.repository.AttachmentRepository;
import com.chatforum.repository.ForumGroupRepository;
import com.chatforum.repository.ForumPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumService {

    private final ForumGroupRepository groupRepo;
    private final ForumPostRepository postRepo;
    private final AttachmentRepository attachmentRepo;

    @Transactional
    public ForumGroup createForumGroup(String groupName, String description, String createdBy, List<String> members) {
        ForumGroup group = new ForumGroup();
        group.setGroupName(groupName != null ? groupName : "New Group");
        group.setDescription(description);
        group.setCreatedBy(createdBy);
        group.setCreatedAt(LocalDateTime.now());
        if (members != null) {
            group.setMembers(new ArrayList<>(members));
        } else {
            group.setMembers(new ArrayList<>());
        }
        return groupRepo.save(group);
    }

    @Transactional(readOnly = true)
    public List<ForumGroup> getAllGroups() {
        try {
            log.info("Fetching all groups from database");
            List<ForumGroup> groups = groupRepo.findAll();
            log.info("Found {} groups", groups.size());
            return groups;
        } catch (Exception e) {
            log.error("Error fetching all groups", e);
            throw new RuntimeException("Failed to fetch groups: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ForumGroup> findGroupsByMember(String username) {
        try {
            log.info("Fetching groups for member: {}", username);
            if (username == null || username.trim().isEmpty()) {
                log.warn("Username is null or empty");
                return new ArrayList<>();
            }

            List<ForumGroup> groups = groupRepo.findGroupsByMember(username.trim());
            log.info("Found {} groups for member: {}", groups.size(), username);
            return groups;
        } catch (Exception e) {
            log.error("Error fetching groups for member: {}", username, e);
            throw new RuntimeException("Failed to fetch user groups: " + e.getMessage());
        }
    }

    @Transactional
    public ForumGroup updateGroupMembers(Long groupId, List<String> newMembers) {
        ForumGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        group.setMembers(newMembers != null ? new ArrayList<>(newMembers) : new ArrayList<>());
        return groupRepo.save(group);
    }

    @Transactional
    public ForumGroup updateGroup(Long groupId, String groupName, String description, List<String> newMembers) {
        ForumGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        if (groupName != null) {
            group.setGroupName(groupName);
        }
        if (description != null) {
            group.setDescription(description);
        }
        group.setMembers(newMembers != null ? new ArrayList<>(newMembers) : new ArrayList<>());
        return groupRepo.save(group);
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        if (!groupRepo.existsById(groupId)) {
            throw new RuntimeException("Group not found: " + groupId);
        }
        groupRepo.deleteById(groupId);
    }

    // ===== POST METHODS =====

    @Transactional
    public ForumPostDto createPost(Long groupId, CreatePostRequest request) {
        ForumGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        ForumPost post = new ForumPost();
        post.setContent(request.getContent());
        post.setCreatedBy(request.getCreatedBy());
        post.setCreatedAt(LocalDateTime.now());
        post.setMessageType(request.getMessageType() != null ? request.getMessageType() : Enums.MessageType.TEXT);
        post.setGroup(group);

        ForumPost saved = postRepo.save(post);

        List<AttachmentDto> dtos = new ArrayList<>();
        if (request.getAttachments() != null) {
            for (AttachmentDto dto : request.getAttachments()) {
                Attachment att = new Attachment();
                att.setFileName(dto.getFileName());
                att.setFileType(dto.getFileType());
                att.setFileSize(dto.getFileSize());
                att.setAttachmentType(dto.getAttachmentType());
                att.setFileData(dto.getFileData());
                att.setCreatedAt(LocalDateTime.now());
                att.setPost(saved);
                attachmentRepo.save(att);
                dtos.add(new AttachmentDto(att));
            }
        }

        return new ForumPostDto(saved.getId(), saved.getContent(), saved.getCreatedBy(),
                saved.getCreatedAt(), saved.getMessageType(), dtos);
    }

    @Transactional(readOnly = true)
    public List<ForumPostDto> getPosts(Long groupId) {
        try {
            return postRepo.findByGroupIdWithAttachments(groupId)
                    .stream()
                    .map(post -> {
                        List<AttachmentDto> dtos = post.getAttachments().stream()
                                .map(AttachmentDto::new)
                                .collect(Collectors.toList());
                        return new ForumPostDto(post.getId(), post.getContent(),
                                post.getCreatedBy(), post.getCreatedAt(),
                                post.getMessageType(), dtos);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching posts for group: {}", groupId, e);
            throw new RuntimeException("Failed to fetch posts: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePost(Long postId, String requester) {
        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getCreatedBy().equals(requester))
            throw new RuntimeException("Cannot delete others' post");
        postRepo.delete(post);
    }

    @Transactional(readOnly = true)
    public byte[] getFile(Long id) {
        Attachment att = attachmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (att.getFileData() == null)
            throw new RuntimeException("File data null");
        return att.getFileData();
    }

    public Enums.AttachmentType detectAttachmentType(MultipartFile file) {
        String type = file.getContentType();
        if (type == null) return Enums.AttachmentType.FILE;
        if (type.startsWith("image/")) return Enums.AttachmentType.IMAGE;
        if (type.startsWith("video/")) return Enums.AttachmentType.VIDEO;
        if (type.startsWith("audio/")) return Enums.AttachmentType.AUDIO;
        return Enums.AttachmentType.FILE;
    }

    // ===== UPLOAD METHOD =====
    @Transactional
    public ForumPostDto createPostWithFiles(
            Long groupId,
            String content,
            String createdBy,
            List<MultipartFile> files,
            String locationJson,
            String eventJson) throws IOException {

        CreatePostRequest request = new CreatePostRequest();
        request.setContent(content);
        request.setCreatedBy(createdBy);

        List<AttachmentDto> attachments = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                attachments.add(new AttachmentDto(
                        null,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize(),
                        detectAttachmentType(file),
                        file.getBytes()
                ));
            }
        }

        if (locationJson != null) {
            attachments.add(new AttachmentDto(
                    null,
                    "location.json",
                    "application/json",
                    (long) locationJson.length(),
                    Enums.AttachmentType.LOCATION,
                    locationJson.getBytes(StandardCharsets.UTF_8)
            ));
        }

        if (eventJson != null) {
            attachments.add(new AttachmentDto(
                    null,
                    "event.json",
                    "application/json",
                    (long) eventJson.length(),
                    Enums.AttachmentType.EVENT,
                    eventJson.getBytes(StandardCharsets.UTF_8)
            ));
        }

        if (!attachments.isEmpty()) request.setAttachments(attachments);

        return createPost(groupId, request);
    }
}