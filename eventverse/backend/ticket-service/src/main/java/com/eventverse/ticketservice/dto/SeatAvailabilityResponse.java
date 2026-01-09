package com.eventverse.ticketservice.dto;

public record SeatAvailabilityResponse(
        Long eventId,
        Integer totalSeats,
        Integer availableSeats
) {}
