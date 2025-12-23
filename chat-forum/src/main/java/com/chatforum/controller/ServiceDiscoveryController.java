package com.chatforum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatforum")
public class ServiceDiscoveryController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/ws-endpoint")
    public Map<String, String> getWebSocketEndpoint() {
        Map<String, String> endpoint = new HashMap<>();

        // Get this service's instance info
        var instances = discoveryClient.getInstances("chat-forum");
        if (!instances.isEmpty()) {
            var instance = instances.get(0);
            String wsUrl = "ws://" + instance.getHost() + ":" + instance.getPort() + "/ws";
            endpoint.put("wsUrl", wsUrl);
            endpoint.put("host", instance.getHost());
            endpoint.put("port", String.valueOf(instance.getPort()));
        } else {
            // Fallback to default
            endpoint.put("wsUrl", "ws://10.2.0.49:8088/ws");
            endpoint.put("host", "10.2.0.49");
            endpoint.put("port", "8088");
        }

        return endpoint;
    }
}