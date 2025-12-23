package com.secureauth.productapi.controller;

import com.secureauth.productapi.dto.JwtResponse;
import com.secureauth.productapi.dto.LoginRequest;
import com.secureauth.productapi.dto.SignupRequest;
import com.secureauth.productapi.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        // Authenticate user & generate JWT + full details
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        // Set JWT as HttpOnly cookie
        Cookie cookie = new Cookie("jwt", jwtResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        // cookie.setSecure(true); // Uncomment if using HTTPS
        response.addCookie(cookie);

        return ResponseEntity.ok(jwtResponse);
    }


    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        String message = authService.registerUser(signupRequest);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }
    @GetMapping("/user/{username}/info")
    public ResponseEntity<JwtResponse> getUserInfo(@PathVariable String username) {
        JwtResponse userInfo = authService.getUserInfoByUsername(username);
        return ResponseEntity.ok(userInfo);
    }
}
