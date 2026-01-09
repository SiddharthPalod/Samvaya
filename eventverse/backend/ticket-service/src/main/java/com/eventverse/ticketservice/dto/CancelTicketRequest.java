package com.eventverse.ticketservice.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record CancelTicketRequest(
        @NotNull UUID ticketId,
        @NotNull Long userId
) { }