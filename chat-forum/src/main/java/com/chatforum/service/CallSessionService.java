// src/main/java/com/chatforum/service/CallSessionService.java
package com.chatforum.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CallSessionService {

    @Data
    public static class CallSession {
        private Long groupId;
        private String admin;
        private Set<String> participants = new HashSet<>();
        private long startedAt = System.currentTimeMillis();
    }

    private final Map<Long, CallSession> activeCalls = new ConcurrentHashMap<>();

    public boolean createCall(Long groupId, String admin) {
        if (activeCalls.containsKey(groupId)) return false; // Already active
        CallSession session = new CallSession();
        session.setGroupId(groupId);
        session.setAdmin(admin);
        activeCalls.put(groupId, session);
        return true;
    }

    public CallSession getCall(Long groupId) {
        return activeCalls.get(groupId);
    }

    public void addParticipant(Long groupId, String username) {
        CallSession session = activeCalls.get(groupId);
        if (session != null) session.getParticipants().add(username);
    }

    public void removeParticipant(Long groupId, String username) {
        CallSession session = activeCalls.get(groupId);
        if (session != null) {
            session.getParticipants().remove(username);
            if (username.equals(session.getAdmin())) {
                // Admin left → end call for all
                endCall(groupId);
            }
        }
    }

    public void endCall(Long groupId) {
        activeCalls.remove(groupId);
        // Broadcast to group: CALL_ENDED
    }

    public boolean isActive(Long groupId) {
        return activeCalls.containsKey(groupId);
    }
}