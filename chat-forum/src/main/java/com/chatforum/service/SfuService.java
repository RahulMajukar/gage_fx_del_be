package com.chatforum.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SfuService {

    @Value("${sfu.server.url:http://10.2.0.49:5173}")
    private String sfuServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory group → router mapping (in prod, use Redis)
    private final Map<Long, String> groupRouterMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sendTransports = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> recvTransports = new ConcurrentHashMap<>();
    private final Map<String, String> producers = new ConcurrentHashMap<>();

    public Map<String, Object> getRouterRtpCapabilities() {
        String url = sfuServerUrl + "/router-rtp";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public Map<String, Object> createSendTransport(Long groupId) {
        // Ensure router exists for group
        String routerId = groupRouterMap.computeIfAbsent(groupId, g -> {
            String url = sfuServerUrl + "/create-router";
            Map<String, Object> body = Map.of("groupId", g);
            ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
            return (String) res.getBody().get("routerId");
        });

        // Create transport
        String url = sfuServerUrl + "/create-transport";
        Map<String, Object> body = Map.of(
                "routerId", routerId,
                "type", "send"
        );
        ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
        Map<String, Object> transport = res.getBody();
        String transportId = (String) transport.get("id");
        sendTransports.put(transportId, transport);
        return transport;
    }

    public Map<String, Object> createRecvTransport(Long groupId) {
        String routerId = groupRouterMap.get(groupId);
        if (routerId == null) {
            throw new IllegalStateException("No active call for group " + groupId);
        }

        String url = sfuServerUrl + "/create-transport";
        Map<String, Object> body = Map.of(
                "routerId", routerId,
                "type", "recv"
        );
        ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
        Map<String, Object> transport = res.getBody();
        String transportId = (String) transport.get("id");
        recvTransports.put(transportId, transport);
        return transport;
    }

    public String createProducer(Long groupId, String kind, Map<String, Object> rtpParameters) {
        String routerId = groupRouterMap.get(groupId);
        if (routerId == null) {
            throw new IllegalStateException("No active call for group " + groupId);
        }

        String url = sfuServerUrl + "/create-producer";
        Map<String, Object> body = Map.of(
                "routerId", routerId,
                "kind", kind,
                "rtpParameters", rtpParameters
        );
        ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
        String producerId = (String) res.getBody().get("producerId");
        producers.put(producerId, groupId.toString());
        return producerId;
    }

    // Create a REAL Mediasoup producer (not just a mock ID)
    public String createRealProducer(Long groupId, String userId, String kind) {
        // 1. Ensure router exists for group
        String routerId = groupRouterMap.computeIfAbsent(groupId, g -> {
            String url = sfuServerUrl + "/create-router";
            Map<String, Object> body = Map.of("groupId", g);
            ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
            return (String) res.getBody().get("routerId");
        });

        // 2. Create transport if needed (store in map)
        String transportId = userId + "-" + groupId;
        if (!sendTransports.containsKey(transportId)) {
            String url = sfuServerUrl + "/create-transport";
            Map<String, Object> body = Map.of("routerId", routerId, "type", "send");
            ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
            sendTransports.put(transportId, res.getBody());
        }

        // 3. Create producer in Mediasoup
        String url = sfuServerUrl + "/create-producer";
        Map<String, Object> body = Map.of(
                "routerId", routerId,
                "kind", kind,
                "rtpParameters", Map.of("codecs", List.of(Map.of(
                        "mimeType", kind.equals("video") ? "video/VP8" : "audio/opus",
                        "clockRate", kind.equals("video") ? 90000 : 48000
                )))
        );
        ResponseEntity<Map> res = restTemplate.postForEntity(url, body, Map.class);
        return (String) res.getBody().get("producerId");
    }
}