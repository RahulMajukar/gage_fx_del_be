package com.GageFx.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

@Component
@Order(-100)
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // Define endpoints that require JWT authentication
    private static final List<String> PROTECTED_ENDPOINTS = Arrays.asList(
        "/api/admin/",
        "/api/secure/",
        "/api/internal/"
    );

    // Define endpoints that are always public (no auth required)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/health",
        "/",
        "/api/auth/",
        "/api/public/",
        "/api/calibration-manager",
        "/api/gages",
        "/api/manufacturers",
        "/api/gage-sub-types",
        "/api/inhouse-calibration-machines",
        "/api/suppliers",
        "/api/service-providers",
        "/api/products",
        "/api/calendar",
        "/api/departments",
        "/api/functions",
        "/api/operations",
        "/api/roles",
        "/api/users", "/api/jobs", "/api/mail", "/api/reallocates", "/api/forum",
            "/api/sfu",
            "/ws/**",
            "/app/**",
            "/topic/**",
            "/user/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethodValue();

        logger.debug("JWT Filter: Processing request to {} with method {}", path, method);

        // Allow OPTIONS requests (CORS preflight)
        if (method.equalsIgnoreCase("OPTIONS")) {
            logger.debug("JWT Filter: Allowing OPTIONS request to {}", path);
            return chain.filter(exchange);
        }

        // Check if endpoint is public
        if (isPublicEndpoint(path)) {
            logger.debug("JWT Filter: Allowing public endpoint access to {}", path);
            return chain.filter(exchange);
        }

        // Check if endpoint requires authentication
        if (isProtectedEndpoint(path)) {
            logger.debug("JWT Filter: Validating JWT for protected endpoint {}", path);
            return validateJWTAndContinue(exchange, chain);
        }

        // For all other endpoints, allow without authentication
        logger.debug("JWT Filter: Allowing unclassified endpoint access to {}", path);
        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isProtectedEndpoint(String path) {
        return PROTECTED_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> validateJWTAndContinue(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("JWT Filter: Missing or invalid Authorization header for protected endpoint");
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.debug("JWT Filter: JWT validation successful");
            return chain.filter(exchange);
        } catch (JwtException e) {
            logger.warn("JWT Filter: JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        logger.warn("JWT Filter: Returning 401 Unauthorized");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
