package com.eventverse.authservice.service;

import com.eventverse.authservice.dto.AuthResponse;
import com.eventverse.authservice.dto.LoginRequest;
import com.eventverse.authservice.dto.RegisterRequest;
import com.eventverse.authservice.dto.UserAdminResponse;
import com.eventverse.authservice.dto.UserAdminUpdateRequest;
import com.eventverse.authservice.entity.User;
import com.eventverse.authservice.repository.UserRepository;
import com.eventverse.authservice.security.JwtService;
import com.eventverse.authservice.dto.UserProfileResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is already registered"
            );
        }

        Instant now = Instant.now();
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        User saved = userRepository.save(user);
        String token = generateTokenForUser(saved);

        return new AuthResponse(saved.getId(),token, "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }
        String token = generateTokenForUser(user);
        return new AuthResponse(user.getId(),token, "Login successful");
    }

    private String generateTokenForUser(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        // subject = email
        return jwtService.generateToken(user.getEmail(), claims);
    }

    public UserProfileResponse getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header"
            );
        }

        String token = authHeader.substring(7); // remove "Bearer "

        if (!jwtService.isValid(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        // We used email as subject
        String email = jwtService.getSubject(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public List<UserProfileResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserProfileResponse(u.getId(), u.getEmail(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<UserAdminResponse> listUsersAdmin() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserAdminResponse(u.getId(), u.getEmail(), u.getPasswordHash(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public UserAdminResponse updateUser(Long id, UserAdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        user.setEmail(request.email());
        user.setUpdatedAt(Instant.now());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        User saved = userRepository.save(user);
        return new UserAdminResponse(saved.getId(), saved.getEmail(), saved.getPasswordHash(), saved.getCreatedAt());
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

}