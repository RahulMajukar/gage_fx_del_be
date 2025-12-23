// src/main/java/com/chatforum/controller/ChatWebSocketController.java
package com.chatforum.controller;

import com.chatforum.dto.CreatePostRequest;
import com.chatforum.dto.ForumPostDto;
import com.chatforum.dto.MessageDto;
import com.chatforum.service.ForumService;
import com.chatforum.service.SfuService;
import com.chatforum.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SfuService sfuService;
    private final ForumService forumService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserPresenceService presenceService;

    // Track active call participants
    private final Map<String, Set<String>> activeCalls = new ConcurrentHashMap<>();

    // ==================== CHAT MESSAGES ====================
    @MessageMapping("/api/send/{groupId}")
    public void sendMessage(@DestinationVariable String groupId, @Payload CreatePostRequest message) {
        try {
            log.info("📨 Received chat message for group {} from {}", groupId, message.getCreatedBy());

            Long groupIdLong = Long.parseLong(groupId);
            ForumPostDto post = forumService.createPost(groupIdLong, message);

            MessageDto response = new MessageDto();
            response.setType("CHAT");
            response.setContent(post.getContent());
            response.setSender(post.getCreatedBy());
            response.setGroupId(groupIdLong);
            response.setPostId(post.getId());
            response.setTimestamp(LocalDateTime.now());
            response.setMessageType(post.getMessageType());
            response.setAttachments(post.getAttachments());

            // Broadcast to ALL users in the group
            messagingTemplate.convertAndSend("/topic/group/" + groupId, response);
            log.info("✅ Broadcasted message to ALL users in group {}", groupId);

        } catch (Exception e) {
            log.error("❌ Error processing chat message:", e);
        }
    }

    // ==================== CALL NOTIFICATIONS ====================
    @MessageMapping("/call/{groupId}")
    public void handleCallAction(@DestinationVariable String groupId, @Payload Map<String, Object> callAction) {
        try {
            String action = (String) callAction.get("action");
            String sender = (String) callAction.get("sender");

            log.info("📞 Call action: {} from {} in group {}", action, sender, groupId);

            // Handle different call actions
            switch (action) {
                case "START_CALL":
                    // Initialize call participants with the caller
                    Set<String> participants = new HashSet<>();
                    participants.add(sender);
                    activeCalls.put(groupId, participants);
                    log.info("🚀 Call started in group {} by {}", groupId, sender);
                    break;

                case "JOIN_CALL":
                    // Add participant to call
                    activeCalls.computeIfAbsent(groupId, k -> new HashSet<>()).add(sender);
                    log.info("👤 {} joined call in group {}", sender, groupId);
                    break;

                case "LEAVE_CALL":
                case "DECLINE_CALL":
                    // Remove participant from call
                    if (activeCalls.containsKey(groupId)) {
                        activeCalls.get(groupId).remove(sender);
                        log.info("🚪 {} left call in group {}", sender, groupId);
                    }
                    break;

                case "END_CALL":
                    // Remove entire call
                    activeCalls.remove(groupId);
                    log.info("📞 Call ended in group {} by {}", groupId, sender);
                    break;
            }

            // Create call message
            MessageDto callMessage = new MessageDto();
            callMessage.setType("CALL_ACTION");
            callMessage.setAction(action);
            callMessage.setSender(sender);
            callMessage.setGroupId(Long.parseLong(groupId));
            callMessage.setTimestamp(LocalDateTime.now());

            if (callAction.containsKey("targetUser")) {
                callMessage.setTargetUser((String) callAction.get("targetUser"));
            }

            // Include current participants in the message
            if (activeCalls.containsKey(groupId)) {
                callMessage.setParticipants(new ArrayList<>(activeCalls.get(groupId)));
            }

            // BROADCAST TO ALL USERS IN THE GROUP (including sender for sync)
            messagingTemplate.convertAndSend("/topic/call/" + groupId, callMessage);

            log.info("✅ Broadcasted call action '{}' to ALL users in group {} ({} participants)",
                    action, groupId, activeCalls.getOrDefault(groupId, Collections.emptySet()).size());

        } catch (Exception e) {
            log.error("❌ Error processing call action:", e);
        }
    }

    // ==================== USER PRESENCE ====================
    @MessageMapping("/presence/online")
    public void markUserOnline(@Payload String username) {
        try {
            presenceService.markOnline(username);
            log.info("👤 User {} is online", username);
        } catch (Exception e) {
            log.error("❌ Error marking user online:", e);
        }
    }

    // ==================== SFU SIGNALING ====================
    @MessageMapping("/sfu")
    public void handleSfuSignal(@Payload Map<String, Object> signal) {
        try {
            String signalType = (String) signal.get("type");
            String userId = (String) signal.get("userId");
            String groupId = (String) signal.get("groupId");

            log.info("📡 SFU signal: {} from {} in group {}", signalType, userId, groupId);

            if ("connect-send-transport".equals(signalType)) {
                Long groupIdLong = Long.parseLong(groupId);

                // Create producers and notify others
                String videoProducerId = sfuService.createRealProducer(groupIdLong, userId, "video");
                String audioProducerId = sfuService.createRealProducer(groupIdLong, userId, "audio");

                // Notify other participants about new producers
                Map<String, Object> videoSignal = Map.of(
                        "type", "new-producer",
                        "userId", userId,
                        "producerId", videoProducerId,
                        "kind", "video"
                );

                Map<String, Object> audioSignal = Map.of(
                        "type", "new-producer",
                        "userId", userId,
                        "producerId", audioProducerId,
                        "kind", "audio"
                );

                messagingTemplate.convertAndSend("/topic/sfu/" + groupId, videoSignal);
                messagingTemplate.convertAndSend("/topic/sfu/" + groupId, audioSignal);

                log.info("🎥 Notified group {} about new producers from {}", groupId, userId);
            }

        } catch (Exception e) {
            log.error("❌ Error handling SFU signal:", e);
        }
    }

    // Get active call participants (for debugging)
    public Map<String, Set<String>> getActiveCalls() {
        return new HashMap<>(activeCalls);
    }

    // Add this method to test WebSocket connection
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String testWebSocket(String message) {
        log.info("✅ WebSocket test received: {}", message);
        return "WebSocket is working! Received: " + message;
    }

    // ==================== VIDEO SIGNALING ====================
    @MessageMapping("/video/{groupId}")
    public void handleVideoSignal(@DestinationVariable String groupId, @Payload Map<String, Object> videoSignal) {
        try {
            String action = (String) videoSignal.get("action");
            String sender = (String) videoSignal.get("sender");
            Object payload = videoSignal.get("payload");

            log.info("🎥 Video signal for group {} from {}: {}", groupId, sender, action);

            // Create video signal message
            MessageDto videoMessage = new MessageDto();
            videoMessage.setType("VIDEO_SIGNAL");
            videoMessage.setAction(action);
            videoMessage.setSender(sender);
            videoMessage.setGroupId(Long.parseLong(groupId));
            videoMessage.setTimestamp(LocalDateTime.now());
            videoMessage.setPayload(payload);

            // Broadcast to ALL users in the group (except sender if needed)
            messagingTemplate.convertAndSend("/topic/video/" + groupId, videoMessage);

            log.info("✅ Broadcasted video signal '{}' to group {}", action, groupId);

        } catch (Exception e) {
            log.error("❌ Error processing video signal:", e);
        }
    }
}