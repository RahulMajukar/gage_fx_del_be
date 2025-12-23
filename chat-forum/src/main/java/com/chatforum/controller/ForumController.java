// src/main/java/com/chatforum/controller/ForumController.java
package com.chatforum.controller;

import com.chatforum.dto.AttachmentDto;
import com.chatforum.dto.CreatePostRequest;
import com.chatforum.dto.ForumPostDto;
import com.chatforum.entity.ForumGroup;
import com.chatforum.enums.Enums;
import com.chatforum.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@Slf4j
public class ForumController {

    private final ForumService forumService;

    // ✅ CREATE GROUP — NO groupId in body
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> groupData) {
        try {
            String groupName = (String) groupData.get("groupName");
            String description = (String) groupData.get("description");
            String createdBy = (String) groupData.get("createdBy");
            List<String> members = (List<String>) groupData.get("members");

            ForumGroup group = forumService.createForumGroup(groupName, description, createdBy, members);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "groupId", group.getGroupId(),
                    "groupName", group.getGroupName()
            ));
        } catch (Exception e) {
            log.error("Error creating group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ NEW: Get groups for a specific user (by username)
    @GetMapping("/groups/all")
    public ResponseEntity<?> getAllGroups() {
        try {
            log.info("Fetching all groups");
            List<ForumGroup> groups = forumService.getAllGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("Error fetching all groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch groups: " + e.getMessage()));
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<?> findGroupsByMember(@RequestParam String username) {
        try {
            log.info("Fetching groups for user: {}", username);
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            List<ForumGroup> groups = forumService.findGroupsByMember(username);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("Error fetching groups for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch user groups: " + e.getMessage()));
        }
    }

    // ✅ UPDATE GROUP
    @PutMapping("/groups/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> updates) {
        try {
            List<String> members = (List<String>) updates.get("members");
            String groupName = (String) updates.get("groupName");
            String description = (String) updates.get("description");
            ForumGroup updated = forumService.updateGroup(groupId, groupName, description, members);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ DELETE GROUP
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        try {
            forumService.deleteGroup(groupId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error deleting group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ CREATE POST (JSON with base64)
    @PostMapping("/groups/{groupId}/posts")
    public ResponseEntity<?> createPost(
            @PathVariable Long groupId,
            @RequestBody CreatePostRequest request) {
        try {
            ForumPostDto post = forumService.createPost(groupId, request);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("Error creating post for group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ CREATE POST WITH FILE UPLOADS
    @PostMapping("/groups/{groupId}/posts/upload")
    public ResponseEntity<?> createPostWithFiles(
            @PathVariable Long groupId,
            @RequestParam("content") String content,
            @RequestParam("createdBy") String createdBy,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "location", required = false) String locationJson,
            @RequestParam(value = "event", required = false) String eventJson
    ) {
        try {
            ForumPostDto post = forumService.createPostWithFiles(groupId, content, createdBy, files, locationJson, eventJson);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("Error creating post with files for group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ GET POSTS
    @GetMapping("/groups/{groupId}/posts")
    public ResponseEntity<?> getPosts(@PathVariable Long groupId) {
        try {
            List<ForumPostDto> posts = forumService.getPosts(groupId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Error fetching posts for group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ DELETE POST
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        @RequestParam String requester) {
        try {
            forumService.deletePost(postId, requester);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error deleting post: {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ DOWNLOAD ATTACHMENT
    @GetMapping("/attachments/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            byte[] data = forumService.getFile(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + id + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            log.error("Error downloading file: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/groups/{groupId}/call/status")
    public ResponseEntity<Map<String, Object>> getCallStatus(@PathVariable Long groupId) {
        // For now: just return placeholder. In production, track active calls in Redis/DB.
        return ResponseEntity.ok(Map.of(
                "active", false,
                "initiator", null,
                "participants", new ArrayList<String>()
        ));
    }
}