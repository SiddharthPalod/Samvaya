package com.eventverse.mediaservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for media service.
 * Allows public access to static files, but requires authentication for uploads.
 * In production, you may want to restrict uploads to authenticated users only.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for file uploads
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to static files (CDN-like behavior)
                        .requestMatchers("/static/**").permitAll()
                        // Allow uploads (can be restricted to authenticated users in production)
                        .requestMatchers("/media/upload").permitAll()
                        // Allow actuator
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll() // Trust API Gateway for auth
                )
                .build();
    }
}
