package com.GageFx.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "API Gateway is running!";
    }

    @GetMapping("/")
    public String root() {
        return "API Gateway - Welcome! Available routes: /api/auth/**, /api/gages/**, /api/products/**, /api/calendar/**";
    }
} 