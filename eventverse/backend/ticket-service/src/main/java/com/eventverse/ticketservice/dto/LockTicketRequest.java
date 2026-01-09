package com.eventverse.ticketservice.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LockTicketRequest(
        @NotNull Long eventId,
        @NotNull Long userId,
        @Min(1) Integer quantity
) { }