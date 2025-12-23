package com.secureauth.productapi.service.interfaces;

public interface EmailService {
    void sendCredentials(String toEmail, String username, String password);
}


