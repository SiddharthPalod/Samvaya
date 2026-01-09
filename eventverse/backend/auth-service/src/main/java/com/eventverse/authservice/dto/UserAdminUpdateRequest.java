package com.eventverse.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserAdminUpdateRequest(
        @Email @NotBlank String email,
        String newPassword // optional; if present, re-hash
) {}

