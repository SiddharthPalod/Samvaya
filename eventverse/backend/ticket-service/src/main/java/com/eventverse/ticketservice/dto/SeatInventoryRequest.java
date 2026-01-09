package com.eventverse.ticketservice.dto;

import jakarta.validation.constraints.Min;

public record SeatInventoryRequest(
        @Min(0) Integer totalSeats,
        Integer availableSeats
) {}

