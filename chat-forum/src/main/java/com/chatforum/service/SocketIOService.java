package com.chatforum.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.SocketIOClient;
import com.chatforum.dto.CallEventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SocketIOService {

    @Autowired
    private SocketIOServer socketIOServer;

    // Store user-group mappings - thread-safe
    private final Map<String, String> userGroupMap = new ConcurrentHashMap<>(); // sessionId -> groupId
    private final Map<String, String> userUsernameMap = new ConcurrentHashMap<>(); // sessionId -> username

    @PostConstruct
    private void autoStartup() {
        start();
    }

    @PreDestroy
    private void autoStop() {
        stop();
    }

    public void start() {
        socketIOServer.addConnectListener(client -> {
            System.out.println("🔌 Client connected: " + client.getSessionId());
        });

        socketIOServer.addDisconnectListener(client -> {
            String sessionId = client.getSessionId().toString();
            String groupId = userGroupMap.remove(sessionId);
            String username = userUsernameMap.remove(sessionId);

            if (groupId != null && username != null) {
                System.out.println("👋 User " + username + " disconnected from group " + groupId);

                // Notify others in the group about user leaving
                Map<String, Object> leaveMessage = new HashMap<>();
                leaveMessage.put("type", "USER_LEFT");
                leaveMessage.put("user", username);
                leaveMessage.put("groupId", groupId);
                leaveMessage.put("timestamp", System.currentTimeMillis());

                String roomName = "call_group_" + groupId;
                Collection<SocketIOClient> clientsInRoom = socketIOServer.getRoomOperations(roomName).getClients();

                for (SocketIOClient roomClient : clientsInRoom) {
                    if (!roomClient.getSessionId().equals(client.getSessionId())) {
                        roomClient.sendEvent("user_left", leaveMessage);
                    }
                }
            }
        });

        // Handle join call group events - IMPROVED VERSION
        socketIOServer.addEventListener("join_call_group", Object.class, (client, data, ackSender) -> {
            try {
                Map<String, Object> jsonData = (Map<String, Object>) data;
                String groupId = getStringValue(jsonData, "groupId");
                String username = getStringValue(jsonData, "username");

                if (groupId == null || username == null) {
                    System.err.println("❌ Missing groupId or username in join_call_group");
                    return;
                }

                String sessionId = client.getSessionId().toString();

                // Store user information
                userGroupMap.put(sessionId, groupId);
                userUsernameMap.put(sessionId, username);

                // Leave previous rooms if any
                client.getAllRooms().forEach(room -> {
                    if (room.startsWith("call_group_")) {
                        client.leaveRoom(room);
                        System.out.println("🚪 User " + username + " left room: " + room);
                    }
                });

                // Join the specific group room
                String roomName = "call_group_" + groupId;
                client.joinRoom(roomName);

                System.out.println("👤 User " + username + " joined call group: " + groupId + " (Room: " + roomName + ")");

                // Get current clients in room for debugging
                Collection<SocketIOClient> clientsInRoom = socketIOServer.getRoomOperations(roomName).getClients();
                System.out.println("👥 Now " + clientsInRoom.size() + " clients in room " + roomName + ": " +
                        clientsInRoom.stream().map(c -> userUsernameMap.getOrDefault(c.getSessionId().toString(), "unknown")).toList());

                // Notify others in the group about new user
                Map<String, Object> joinMessage = new HashMap<>();
                joinMessage.put("type", "USER_JOINED");
                joinMessage.put("user", username);
                joinMessage.put("groupId", groupId);
                joinMessage.put("timestamp", System.currentTimeMillis());
                joinMessage.put("roomClientsCount", clientsInRoom.size());

                // Send to all clients in the room EXCEPT the new joiner
                for (SocketIOClient roomClient : clientsInRoom) {
                    if (!roomClient.getSessionId().equals(client.getSessionId())) {
                        roomClient.sendEvent("user_joined", joinMessage);
                        System.out.println("📤 Sent user_joined to: " +
                                userUsernameMap.getOrDefault(roomClient.getSessionId().toString(), "unknown"));
                    }
                }

                System.out.println("📢 Notified " + (clientsInRoom.size() - 1) + " clients about user join");

            } catch (Exception e) {
                System.err.println("❌ Error parsing join_call_group data: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Handle call events - IMPROVED VERSION
        socketIOServer.addEventListener("call_event", Object.class, (client, data, ackSender) -> {
            try {
                Map<String, Object> jsonData = (Map<String, Object>) data;

                // Debug logging
                System.out.println("🔍 Raw call_event data received: " + jsonData);

                // Safely extract values
                String groupId = getStringValue(jsonData, "groupId");
                String type = getStringValue(jsonData, "type");
                String sender = getStringValue(jsonData, "sender");
                String timestamp = getStringValue(jsonData, "timestamp");

                if (groupId == null || type == null || sender == null) {
                    System.err.println("❌ Missing required fields in call_event");
                    return;
                }

                String sessionId = client.getSessionId().toString();
                String userGroup = userGroupMap.get(sessionId);
                String currentUsername = userUsernameMap.get(sessionId);

                // Verify user is in the correct group
                if (!groupId.equals(userGroup)) {
                    System.err.println("❌ User " + currentUsername + " not in group " + groupId);
                    return;
                }

                System.out.println("📞 Call event from " + sender + " in group " + groupId + ": " + type);

                String roomName = "call_group_" + groupId;
                Collection<SocketIOClient> clients = socketIOServer.getRoomOperations(roomName).getClients();
                int clientCount = clients.size();

                System.out.println("👥 Broadcasting to " + clientCount + " clients in room: " + roomName +
                        " - Users: " + clients.stream()
                        .map(c -> userUsernameMap.getOrDefault(c.getSessionId().toString(), "unknown"))
                        .toList());

                // Create clean DTO
                CallEventDTO callEventDTO = new CallEventDTO(type, sender, groupId, timestamp);

                // Create additionalData safely
                Map<String, Object> cleanAdditionalData = new HashMap<>();
                for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                        if (!"type".equals(key) && !"sender".equals(key) && !"groupId".equals(key) && !"timestamp".equals(key)) {
                            cleanAdditionalData.put(key, value);
                        }
                    }
                }

                callEventDTO.setAdditionalData(cleanAdditionalData);

                // Broadcast to all clients in the room EXCEPT sender
                int sentCount = 0;
                for (SocketIOClient roomClient : clients) {
                    if (!roomClient.getSessionId().equals(client.getSessionId())) {
                        roomClient.sendEvent("call_event", callEventDTO);
                        String targetUser = userUsernameMap.getOrDefault(roomClient.getSessionId().toString(), "unknown");
                        System.out.println("📤 Sent call_event to: " + targetUser);
                        sentCount++;
                    }
                }

                System.out.println("📢 Broadcasted call event to " + sentCount + " clients in group: " + groupId);

            } catch (Exception e) {
                System.err.println("❌ Error handling call_event: " + e.getMessage());
                e.printStackTrace();
            }
        });

        socketIOServer.start();
        System.out.println("✅ Socket.IO server started on port " + socketIOServer.getConfiguration().getPort());
    }

    // Helper method to safely extract String values
    private String getStringValue(Map<String, Object> data, String key) {
        if (data == null || key == null) {
            return null;
        }
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    public void stop() {
        if (socketIOServer != null) {
            socketIOServer.stop();
        }
    }
}