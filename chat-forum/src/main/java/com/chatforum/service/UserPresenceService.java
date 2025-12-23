package com.chatforum.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceService {
    private final Map<String, Long> onlineUsers = new ConcurrentHashMap<>();

    public void markOnline(String username) {
        if (username != null && !username.trim().isEmpty()) {
            onlineUsers.put(username, System.currentTimeMillis());
        }
    }

    public boolean isOnline(String username) {
        Long lastSeen = onlineUsers.get(username);
        if (lastSeen == null) return false;
        return (System.currentTimeMillis() - lastSeen) < 60_000; // 60 sec timeout
    }
}