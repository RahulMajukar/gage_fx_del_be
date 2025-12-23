package com.secureauth.productapi.service.interfaces;

import com.secureauth.productapi.dto.JwtResponse;
import com.secureauth.productapi.dto.LoginRequest;
import com.secureauth.productapi.dto.SignupRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    String registerUser(SignupRequest signupRequest);
    JwtResponse getUserInfoByUsername(String username);

}
