package com.GageFx.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsGlobalConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Allow frontend origins
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://10.2.0.95:5173",
                "https://gagefxupdated.netlify.app"
        ));
        // Allow HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Allow all headers (or specify if needed)
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        // Allow credentials (cookies, auth headers)
        corsConfig.setAllowCredentials(true);
        // Expose critical headers to frontend
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        // Cache preflight for 1 hour
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply to all routes

        return new CorsWebFilter(source);
    }
}