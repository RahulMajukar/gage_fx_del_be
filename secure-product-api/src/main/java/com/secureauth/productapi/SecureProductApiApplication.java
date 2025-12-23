package com.secureauth.productapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SecureProductApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SecureProductApiApplication.class, args);
	}
}
