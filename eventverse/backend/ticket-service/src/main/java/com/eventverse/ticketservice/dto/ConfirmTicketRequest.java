package com.eventverse.ticketservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmTicketRequest(
        @NotNull UUID ticketId,
        @NotNull Long userId,
        @NotBlank String idempotencyKey
) { }