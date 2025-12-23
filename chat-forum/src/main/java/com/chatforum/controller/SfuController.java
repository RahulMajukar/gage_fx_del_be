package com.chatforum.controller;

import com.chatforum.service.SfuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sfu")
@RequiredArgsConstructor
public class SfuController {

    private final SfuService sfuService;

    @GetMapping("/router-rtp")
    public ResponseEntity<Map<String, Object>> getRouterRtpCapabilities() {
        return ResponseEntity.ok(sfuService.getRouterRtpCapabilities());
    }

    @GetMapping("/send-transport")
    public ResponseEntity<Map<String, Object>> createSendTransport(@RequestParam Long groupId) {
        return ResponseEntity.ok(sfuService.createSendTransport(groupId));
    }

    @GetMapping("/recv-transport")
    public ResponseEntity<Map<String, Object>> createRecvTransport(@RequestParam Long groupId) {
        return ResponseEntity.ok(sfuService.createRecvTransport(groupId));
    }

    @PostMapping("/produce")
    public ResponseEntity<String> createProducer(@RequestBody Map<String, Object> payload) {
        Long groupId = ((Number) payload.get("groupId")).longValue();
        String kind = (String) payload.get("kind");
        Map<String, Object> rtpParameters = (Map<String, Object>) payload.get("rtpParameters");
        String producerId = sfuService.createProducer(groupId, kind, rtpParameters);
        return ResponseEntity.ok(producerId);
    }
}