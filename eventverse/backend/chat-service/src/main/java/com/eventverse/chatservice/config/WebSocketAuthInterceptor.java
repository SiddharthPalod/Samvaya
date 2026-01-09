package com.eventverse.chatservice.config;

import com.eventverse.chatservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Interceptor to validate JWT and extract user information during WebSocket handshake.
 * Validates JWT token from Authorization header or query parameter.
 */
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            
            // Try to get JWT token from Authorization header first
            String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            String token = null;
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                // Fallback: try query parameter (for SockJS compatibility)
                token = httpRequest.getParameter("token");
            }
            
            // Validate JWT token
            if (token == null || !jwtService.isValid(token)) {
                // Reject handshake if token is invalid
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            // Extract user information from token
            Long userId = jwtService.getUserId(token);
            String userEmail = jwtService.getEmail(token);
            String roomId = httpRequest.getParameter("roomId");
            
            if (userId != null) {
                attributes.put("userId", userId.toString());
            } else {
                // If we can't extract userId, reject
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            if (userEmail != null) {
                attributes.put("userEmail", userEmail);
            }
            
            if (roomId != null && !roomId.isEmpty()) {
                attributes.put("roomId", roomId);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
