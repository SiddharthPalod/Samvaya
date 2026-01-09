package com.eventverse.apigateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    @Value("${admin.bypass.token:}")
    private String configuredBypassToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // 0. Allow OPTIONS requests (CORS preflight) to pass through
        // The CorsWebFilter will handle adding CORS headers
        // We just need to make sure OPTIONS requests don't get blocked
        if (method.equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        // 1. Public routes – no auth
        if (isPublicRoute(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Read Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 2a. Admin bypass token for operator consoles (e.g., manager frontend)
        if (isBypassToken(authHeader)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", "0")
                    .header("X-User-Email", "admin@local")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // 3. Validate token
        if (!jwtService.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. Extract user info, inject into headers for downstream services
        Long userId = jwtService.getUserId(token);
        String email = jwtService.getEmail(token);

        // Preserve the Authorization header for downstream services (especially auth-service)
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId != null ? userId.toString() : "")
                .header("X-User-Email", email != null ? email : "")
                .header(HttpHeaders.AUTHORIZATION, authHeader) // Preserve Authorization header
                .build();

        return chain.filter(
                exchange.mutate().request(mutatedRequest).build()
        );
    }

    private boolean isBypassToken(String authHeader) {
        if (configuredBypassToken == null || configuredBypassToken.isBlank()) {
            return false;
        }
        String expected = "Bearer " + configuredBypassToken;
        return expected.equals(authHeader);
    }

    private boolean isPublicRoute(String path, String method) {
        // Auth endpoints are always public
        if (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/ping")) {
            return true;
        }
        
        // Actuator endpoints are public
        if (path.startsWith("/actuator")) {
            return true;
        }
        
        // GET requests to /events are public (viewing events doesn't require auth)
        if (path.startsWith("/events") && method.equals("GET")) {
            return true;
        }
        
        // Webhook endpoints are public (partners register webhooks without user accounts)
        if (path.startsWith("/webhooks")) {
            return true;
        }
        
        // Note: /notifications/** route exists in gateway config but no endpoints exist yet
        // When endpoints are added, they should be evaluated for public vs authenticated access
        
        // All other routes require JWT authentication
        return false;
    }

    @Override
    public int getOrder() {
        // run early
        return -1;
    }
}
