package com.calendar.controller;

import com.calendar.dto.JwtResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "http://localhost:8081/api/auth")
public interface AuthFeignClient {

    @GetMapping("/user/{username}/info")
    JwtResponseDTO getUserInfo(@PathVariable("username") String username);

}
