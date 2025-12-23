package com.GageFx.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .cors().and()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/health", "/").permitAll() // allow health check endpoints
                .pathMatchers("/api/auth/**").permitAll() // allow auth endpoints without JWT
                .pathMatchers("/api/public/**").permitAll()
                .pathMatchers("/api/departments/**").permitAll()
                .pathMatchers("/api/functions/**").permitAll()
                .pathMatchers("/api/operations/**").permitAll()
                .pathMatchers("/api/roles/**").permitAll()
                .pathMatchers("/api/users/**").permitAll()
                .pathMatchers("/api/gages/**").permitAll()
                .pathMatchers("/api/gage-sub-types/**").permitAll()
                .pathMatchers("/api/inhouse-calibration-machines/**").permitAll()
                .pathMatchers("/api/gage-types/**").permitAll()
                .pathMatchers("/api/manufacturers/**").permitAll()
                .pathMatchers("/api/suppliers/**").permitAll()
                .pathMatchers("/api/service-providers/**").permitAll()
                .pathMatchers("/api/products/**").permitAll()
                .pathMatchers("/api/calendar/**").permitAll()
                .pathMatchers("/api/gage-issues/**").permitAll()
                .pathMatchers("/api/calibration-manager/**").permitAll()
                .pathMatchers("/api/jobs/**").permitAll()
                .pathMatchers("/api/mail/**").permitAll()
                .pathMatchers("/api/mail/**").permitAll()
                .pathMatchers("/api/reallocates/**").permitAll()
                .pathMatchers("/api/forum/**").permitAll()
                .pathMatchers("/ws/**").permitAll()
                // Allow all other endpoints without requiring authentication
                .anyExchange().permitAll()
                .and()
                .httpBasic().disable() // Disable basic authentication
                .formLogin().disable() // Disable form login
                .build();
    }
}