package com.eventverse.chatservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for chat service.
 * WebSocket authentication is handled via headers (X-User-Id) injected by API Gateway.
 * This service trusts the API Gateway to have already validated JWT tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for WebSocket and API endpoints
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow WebSocket handshake endpoints
                        .requestMatchers("/ws/**", "/ws/chat/**").permitAll()
                        // Allow actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // All other requests require authentication (handled by API Gateway)
                        .anyRequest().permitAll() // Trust API Gateway for auth
                )
                .build();
    }
}
