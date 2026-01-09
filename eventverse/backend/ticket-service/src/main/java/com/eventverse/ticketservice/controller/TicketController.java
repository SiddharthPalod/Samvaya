package com.eventverse.ticketservice.controller;
import com.eventverse.ticketservice.dto.*;
import com.eventverse.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/lock")
    public ResponseEntity<TicketResponse> lock(@Valid @RequestBody LockTicketRequest request) {
        TicketResponse response = ticketService.lockTickets(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<TicketResponse> confirm(
            @Valid @RequestBody ConfirmTicketRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader
    ) {
        TicketResponse response = ticketService.confirmTicket(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<TicketResponse> cancel(@Valid @RequestBody CancelTicketRequest request) {
        TicketResponse response = ticketService.cancelTicket(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<java.util.List<TicketResponse>> myTickets(
            @RequestHeader("X-User-Id") Long userId
    ) {
        java.util.List<TicketResponse> tickets = ticketService.listTicketsForUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{eventId}/availability")
    public SeatAvailabilityResponse availability(@PathVariable Long eventId) {
        return ticketService.getSeatAvailability(eventId);
    }
}
