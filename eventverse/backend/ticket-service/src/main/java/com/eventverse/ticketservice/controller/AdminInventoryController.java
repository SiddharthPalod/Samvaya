package com.eventverse.ticketservice.controller;

import com.eventverse.ticketservice.domain.SeatInventory;
import com.eventverse.ticketservice.dto.SeatInventoryRequest;
import com.eventverse.ticketservice.repository.SeatInventoryRepository;
import com.eventverse.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Super-admin endpoints for managing seat inventory.
 */
@RestController
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final SeatInventoryRepository seatInventoryRepository;
    private final TicketService ticketService;

    public AdminInventoryController(SeatInventoryRepository seatInventoryRepository,
                                    TicketService ticketService) {
        this.seatInventoryRepository = seatInventoryRepository;
        this.ticketService = ticketService;
    }

    @GetMapping("/{eventId}")
    public SeatInventory get(@PathVariable Long eventId) {
        return seatInventoryRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for event " + eventId));
    }

    @PutMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public SeatInventory upsert(@PathVariable Long eventId, @Valid @RequestBody SeatInventoryRequest request) {
        return ticketService.upsertInventory(eventId, request.totalSeats(), request.availableSeats());
    }
}

