package com.chatforum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@RestController
@RequestMapping("/api/forum")
public class CallNotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, CallInfo> activeCalls = new ConcurrentHashMap<>();

    @PostMapping("/call-notification")
    public ResponseEntity<String> handleCallNotification(@RequestBody CallNotification request) {
        try {
            String callKey = request.getGroupId() + "-call";
            System.out.println("📞 Received call notification: " + request.getAction() + " from " + request.getCaller() + " in group " + request.getGroupId());

            switch (request.getAction()) {
                case "START_CALL":
                    CallInfo callInfo = new CallInfo(
                            request.getCaller(),
                            request.getCallerName(),
                            true
                    );
                    activeCalls.put(callKey, callInfo);

                    // Broadcast to all group members EXCEPT the caller
                    CallBroadcast broadcast = new CallBroadcast(request, "INCOMING_CALL", callInfo);
                    messagingTemplate.convertAndSend(
                            "/topic/call/" + request.getGroupId(),
                            broadcast
                    );
                    System.out.println("📢 Broadcasted INCOMING_CALL to group: " + request.getGroupId());
                    break;

                case "JOIN_CALL":
                    CallInfo existingCall = activeCalls.get(callKey);
                    if (existingCall != null) {
                        existingCall.addParticipant(request.getCaller());
                        // Broadcast join event to all
                        messagingTemplate.convertAndSend(
                                "/topic/call/" + request.getGroupId(),
                                new CallBroadcast(request, "USER_JOINED", existingCall)
                        );
                        System.out.println("📢 User " + request.getCaller() + " joined call in group " + request.getGroupId());
                    }
                    break;

                case "END_CALL":
                case "DECLINE_CALL":
                    CallInfo endedCall = activeCalls.remove(callKey);
                    if (endedCall != null) {
                        // Broadcast call end to all
                        messagingTemplate.convertAndSend(
                                "/topic/call/" + request.getGroupId(),
                                new CallBroadcast(request, "CALL_ENDED", endedCall)
                        );
                        System.out.println("📢 Call ended in group " + request.getGroupId());
                    }
                    break;

                default:
                    return ResponseEntity.badRequest().body("Unknown call action: " + request.getAction());
            }

            return ResponseEntity.ok("Call notification processed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing call notification: " + e.getMessage());
        }
    }

    @GetMapping("/active-calls")
    public ResponseEntity<CallInfo> getActiveCall(@RequestParam String groupId) {
        String callKey = groupId + "-call";
        CallInfo callInfo = activeCalls.get(callKey);

        if (callInfo != null && callInfo.isActive()) {
            return ResponseEntity.ok(callInfo);
        } else {
            return ResponseEntity.ok(new CallInfo(null, null, false));
        }
    }

    // ==================== WebSocket Handlers ====================

    @MessageMapping("/call.{groupId}")
    public void handleCallSignal(@Payload CallSignal signal, @DestinationVariable String groupId) {
        System.out.println("📡 WebSocket signal received for group " + groupId + ": " + signal.getType() + " from " + signal.getFrom());
        // Relay signaling messages between clients
        messagingTemplate.convertAndSend("/topic/call/" + groupId, signal);
    }

    // Add video signaling endpoint
    @MessageMapping("/video.{groupId}")
    public void handleVideoSignal(@Payload VideoSignal signal, @DestinationVariable String groupId) {
        System.out.println("🎥 Video signal received for group " + groupId + ": " + signal.getAction() + " from " + signal.getSender());
        // Relay video signaling messages between clients
        messagingTemplate.convertAndSend("/topic/video/" + groupId, signal);
    }

    // ==================== DTOs ====================

    public static class CallNotification {
        private String groupId;
        private String action;
        private String caller;
        private String callerName;
        private String targetUser;
        private String timestamp;

        // Getters and Setters
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getCaller() { return caller; }
        public void setCaller(String caller) { this.caller = caller; }
        public String getCallerName() { return callerName; }
        public void setCallerName(String callerName) { this.callerName = callerName; }
        public String getTargetUser() { return targetUser; }
        public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class CallInfo {
        private String caller;
        private String callerName;
        private boolean active;
        private Set<String> participants = new ConcurrentSkipListSet<>();
        private long startTime;

        public CallInfo(String caller, String callerName, boolean active) {
            this.caller = caller;
            this.callerName = callerName;
            this.active = active;
            if (caller != null) {
                this.participants.add(caller);
            }
            this.startTime = System.currentTimeMillis();
        }

        public CallInfo() {}

        // Getters and Setters
        public String getCaller() { return caller; }
        public void setCaller(String caller) { this.caller = caller; }
        public String getCallerName() { return callerName; }
        public void setCallerName(String callerName) { this.callerName = callerName; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Set<String> getParticipants() { return participants; }
        public void setParticipants(Set<String> participants) { this.participants = participants; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }

        public void addParticipant(String userId) {
            participants.add(userId);
        }

        public void removeParticipant(String userId) {
            participants.remove(userId);
        }
    }

    public static class CallBroadcast {
        private CallNotification notification;
        private String eventType;
        private CallInfo callInfo;
        private long timestamp;

        public CallBroadcast(CallNotification notification, String eventType, CallInfo callInfo) {
            this.notification = notification;
            this.eventType = eventType;
            this.callInfo = callInfo;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public CallNotification getNotification() { return notification; }
        public void setNotification(CallNotification notification) { this.notification = notification; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public CallInfo getCallInfo() { return callInfo; }
        public void setCallInfo(CallInfo callInfo) { this.callInfo = callInfo; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class CallSignal {
        private String type;
        private String from;
        private Object data;
        private String target;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }

    public static class VideoSignal {
        private String type;
        private String action;
        private String groupId;
        private String sender;
        private Object payload;
        private String timestamp;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}