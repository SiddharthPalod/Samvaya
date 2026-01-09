package com.eventverse.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private Instant createdAt;
}