package com.eventverse.authservice.controller;
import org.springframework.web.bind.annotation.*;
import com.eventverse.authservice.dto.AuthResponse;
import com.eventverse.authservice.dto.LoginRequest;
import com.eventverse.authservice.dto.RegisterRequest;
import com.eventverse.authservice.dto.UserProfileResponse;
import com.eventverse.authservice.dto.UserAdminResponse;
import com.eventverse.authservice.dto.UserAdminUpdateRequest;
import com.eventverse.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "auth-service-ok";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        UserProfileResponse profile = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserProfileResponse>> listUsers(
            @RequestHeader(name = "X-Admin-Superuser", required = false) String superuserHeader
    ) {
        if (!"true".equalsIgnoreCase(superuserHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(authService.listUsers());
    }

    @GetMapping("/admin/users/full")
    public ResponseEntity<List<UserAdminResponse>> listUsersFull(
            @RequestHeader(name = "X-Admin-Superuser", required = false) String superuserHeader
    ) {
        if (!"true".equalsIgnoreCase(superuserHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(authService.listUsersAdmin());
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<UserAdminResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserAdminUpdateRequest request,
            @RequestHeader(name = "X-Admin-Superuser", required = false) String superuserHeader
    ) {
        if (!"true".equalsIgnoreCase(superuserHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader(name = "X-Admin-Superuser", required = false) String superuserHeader
    ) {
        if (!"true".equalsIgnoreCase(superuserHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
