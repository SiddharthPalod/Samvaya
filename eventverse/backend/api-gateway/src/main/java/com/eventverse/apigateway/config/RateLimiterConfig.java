package com.eventverse.apigateway.config;

import com.eventverse.apigateway.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver(JwtService jwtService) {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String ip = extractIp(exchange);

            // 1) For /auth/login -> always rate limit per IP (anti-bruteforce)
            if (path.startsWith("/auth/login")) {
                return Mono.just("ip:" + ip);
            }

            // 2) For others: try JWT -> per-user
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtService.isValid(token)) {
                        Claims claims = jwtService.getClaims(token);
                        Object userId = claims.get("userId");
                        if (userId != null) {
                            return Mono.just("user:" + userId.toString());
                        }
                    }
                } catch (Exception ignored) {
                    // fall back to IP
                }
            }

            // 3) Fallback: IP-based
            return Mono.just("ip:" + ip);
        };
    }

    private String extractIp(ServerWebExchange exchange) {
        if (exchange.getRequest().getRemoteAddress() != null &&
                exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress();
        }
        return "unknown";
    }
}
