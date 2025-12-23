package com.GageFx.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/public")
public class GeocodeController {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "GageFX/1.0 (support@gagefx.local)")
            .build();

    @GetMapping("/geocode")
    public Mono<ResponseEntity<String>> reverseGeocode(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) {
        String uri = String.format("/reverse?format=json&lat=%s&lon=%s", lat, lon);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
    }
}


