package com.eventverse.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * CORS GlobalFilter for Spring Cloud Gateway 4.x (2023.0.3)
 * 
 * This filter handles CORS for Spring Boot 3.2.5 and Spring Cloud Gateway 4.x.
 * It ensures CORS headers are added before any other processing.
 * 
 * Reference: 
 * - https://docs.spring.io/spring-cloud-gateway/reference/4.1/spring-cloud-gateway/cors-configuration.html
 * - https://spring.io/projects/spring-cloud-gateway
 */
@Component
public class CorsConfig implements GlobalFilter, Ordered {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        // Localhost – frontend and manager on host
        "http://localhost:3000",
        "http://localhost:3001",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",

        // Docker service hostnames – when frontend/manager run inside Docker
        "http://frontend:3000",
        "http://manager:3001",

        // Next.js exposes a network URL (169.254.x.x on Windows) when opened from another device.
        // When the app is accessed via that address the Origin matches this IP, so we need to allow it
        // in dev to avoid browser-blocked CORS preflight failures.
        "http://169.254.16.205:3000",
        "http://169.254.16.205:3001"
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders requestHeaders = request.getHeaders();
        HttpHeaders responseHeaders = response.getHeaders();
        String origin = requestHeaders.getFirst(HttpHeaders.ORIGIN);
        String path = request.getPath().value();
        
        // Handle preflight OPTIONS request - MUST complete here, never forward
        if (request.getMethod() == HttpMethod.OPTIONS) {
            // Always respond to OPTIONS with CORS headers if origin is present and allowed
            if (origin != null && isAllowedOrigin(origin)) {
                // CRITICAL: Set all CORS headers BEFORE any other operations
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", ALLOWED_METHODS));
                
                String requestedHeaders = requestHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
                if (requestedHeaders != null && !requestedHeaders.isEmpty()) {
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestedHeaders);
                } else {
                    responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                }
                
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            } else if (origin != null) {
                // Origin present but not in allowed list - log for debugging
                System.err.println("CORS: Origin not allowed: " + origin + " for path: " + path);
            } else {
                // No origin header - log for debugging
                System.err.println("CORS: No origin header in OPTIONS request for path: " + path);
            }
            
            // Set status code and complete response immediately
            response.setStatusCode(HttpStatus.OK);
            responseHeaders.set(HttpHeaders.CONTENT_LENGTH, "0");
            return response.setComplete();
        }

        // For actual requests, add CORS headers BEFORE forwarding
        // These will be preserved even after route filters remove downstream CORS headers
        if (origin != null && isAllowedOrigin(origin)) {
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Type");
        }

        return chain.filter(exchange);
    }

    private boolean isAllowedOrigin(String origin) {
        return ALLOWED_ORIGINS.contains(origin);
    }

    @Override
    public int getOrder() {
        // Run before JWT filter (order -1) and before routing
        return -100;
    }
}

