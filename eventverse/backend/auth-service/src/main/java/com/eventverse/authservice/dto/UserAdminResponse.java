package com.eventverse.authservice.dto;

import java.time.Instant;

public record UserAdminResponse(
        Long id,
        String email,
        String passwordHash,
        Instant createdAt
) {}

