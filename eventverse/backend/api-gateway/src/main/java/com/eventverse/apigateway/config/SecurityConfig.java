package com.eventverse.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // We are an API gateway, no browser forms => disable CSRF
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS is handled by CorsConfig GlobalFilter, not here (to avoid duplicate headers)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Everything is permitted at Spring Security level.
                        // JWT auth is enforced in JwtAuthenticationFilter.
                        .anyExchange().permitAll()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // Disable default security filters that might interfere
                .headers(ServerHttpSecurity.HeaderSpec::disable)
                .build();
    }
}
