package com.eventverse.ticketservice.dto;
import com.eventverse.ticketservice.domain.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record TicketResponse(
        UUID id,
        Long eventId,
        Long userId,
        TicketStatus status,
        BigDecimal price,
        Integer quantity,
        Instant lockedAt,
        Instant lockExpiresAt
) { }
