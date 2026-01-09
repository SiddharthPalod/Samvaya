package com.eventverse.ticketservice.service;
import com.eventverse.ticketservice.dto.LockTicketRequest;
import com.eventverse.ticketservice.dto.TicketResponse;
import com.eventverse.ticketservice.dto.ConfirmTicketRequest;
import com.eventverse.ticketservice.dto.CancelTicketRequest;
import com.eventverse.ticketservice.dto.SeatAvailabilityResponse;
import com.eventverse.ticketservice.domain.SeatInventory;
import com.eventverse.ticketservice.domain.Ticket;
import com.eventverse.ticketservice.domain.TicketStatus;
import com.eventverse.ticketservice.repository.SeatInventoryRepository;
import com.eventverse.ticketservice.repository.TicketRepository;
import com.eventverse.ticketservice.messaging.TicketEventProducer;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final EventPricingClient eventPricingClient;
    private final TicketEventProducer ticketEventProducer;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    public TicketService(TicketRepository ticketRepository,
                         SeatInventoryRepository seatInventoryRepository,
                         EventPricingClient eventPricingClient,
                         TicketEventProducer ticketEventProducer) {
        this.ticketRepository = ticketRepository;
        this.seatInventoryRepository = seatInventoryRepository;
        this.eventPricingClient = eventPricingClient;
        this.ticketEventProducer = ticketEventProducer;
    }

    @Transactional
    public TicketResponse lockTickets(LockTicketRequest request) {
        SeatInventory inventory = seatInventoryRepository.findById(request.eventId())
                .orElseThrow(() -> new IllegalArgumentException("No inventory configured for event"));

        if (inventory.getAvailableSeats() < request.quantity()) {
            throw new IllegalStateException("Not enough seats available");
        }
        inventory.setAvailableSeats(inventory.getAvailableSeats() - request.quantity());
        seatInventoryRepository.save(inventory);

            BigDecimal pricePerSeat = eventPricingClient.getPriceForEvent(request.eventId());
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(request.quantity()));

        Instant now = Instant.now();
        Ticket ticket = new Ticket();
        ticket.setEventId(request.eventId());
        ticket.setUserId(request.userId());
        ticket.setQuantity(request.quantity());
        ticket.setPrice(totalPrice);
        ticket.setStatus(TicketStatus.LOCKED);
        ticket.setLockedAt(now);
        ticket.setLockExpiresAt(now.plus(LOCK_DURATION));

        ticket = ticketRepository.save(ticket);

        return new TicketResponse(
                ticket.getId(),
                ticket.getEventId(),
                ticket.getUserId(),
                ticket.getStatus(),
                ticket.getPrice(),
                ticket.getQuantity(),
                ticket.getLockedAt(),
                ticket.getLockExpiresAt()
        );
    }

    @Transactional
    public List<TicketResponse> listTicketsForUser(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tickets.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse confirmTicket(ConfirmTicketRequest request) {
        // Idempotency check
        var existingByKey = ticketRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingByKey.isPresent()) {
            Ticket t = existingByKey.get();
            return new TicketResponse(
                    t.getId(), t.getEventId(), t.getUserId(), t.getStatus(),
                    t.getPrice(), t.getQuantity(), t.getLockedAt(), t.getLockExpiresAt()
            );
        }
        Ticket ticket = ticketRepository.findByIdAndUserId(request.ticketId(), request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found for user"));

        if (ticket.getStatus() != TicketStatus.LOCKED) {
            throw new IllegalStateException("Ticket is not in LOCKED state");
        }

        if (ticket.getLockExpiresAt().isBefore(Instant.now())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            restoreSeats(ticket);
            throw new IllegalStateException("Ticket lock has expired");
        }

        // if you had real payments, this is where you’d integrate
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setIdempotencyKey(request.idempotencyKey());
        ticket = ticketRepository.save(ticket);
        
        // Publish ticket confirmed event to Kafka
        Map<String, Object> payload = new HashMap<>();
        payload.put("ticketId", ticket.getId().toString());
        payload.put("eventId", ticket.getEventId());
        payload.put("userId", ticket.getUserId());
        payload.put("quantity", ticket.getQuantity());
        payload.put("price", ticket.getPrice().toString());
        ticketEventProducer.publishTicketConfirmed(
            ticket.getId().toString(),
            ticket.getEventId().toString(),
            ticket.getUserId(),
            ticket.getPrice().longValue(),
            payload
        );
        
        return new TicketResponse(
                ticket.getId(), ticket.getEventId(), ticket.getUserId(), ticket.getStatus(),
                ticket.getPrice(), ticket.getQuantity(), ticket.getLockedAt(), ticket.getLockExpiresAt()
        );
    }

    private void restoreSeats(Ticket ticket) {
        SeatInventory inventory = seatInventoryRepository.findById(ticket.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("No inventory for event"));
        inventory.setAvailableSeats(inventory.getAvailableSeats() + ticket.getQuantity());
        seatInventoryRepository.save(inventory);
    }

    @Transactional
    public TicketResponse cancelTicket(CancelTicketRequest request) {
        Ticket ticket = ticketRepository.findByIdAndUserId(request.ticketId(), request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found for user"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            return toResponse(ticket);
        }

        if (ticket.getStatus() == TicketStatus.CONFIRMED ||
                ticket.getStatus() == TicketStatus.LOCKED) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticketRepository.save(ticket);
            restoreSeats(ticket);
            
            // Publish ticket cancelled event to Kafka
            Map<String, Object> payload = new HashMap<>();
            payload.put("ticketId", ticket.getId().toString());
            payload.put("eventId", ticket.getEventId());
            payload.put("userId", ticket.getUserId());
            payload.put("quantity", ticket.getQuantity());
            payload.put("price", ticket.getPrice().toString());
            ticketEventProducer.publishTicketCancelled(
                ticket.getId().toString(),
                ticket.getEventId().toString(),
                ticket.getUserId(),
                ticket.getPrice().longValue(),
                payload
            );
        }

        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getEventId(), t.getUserId(), t.getStatus(),
                t.getPrice(), t.getQuantity(), t.getLockedAt(), t.getLockExpiresAt()
        );
    }

    @Transactional
    public SeatAvailabilityResponse getSeatAvailability(Long eventId) {
        SeatInventory inventory = seatInventoryRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("No inventory configured for event " + eventId));
        return new SeatAvailabilityResponse(
                inventory.getEventId(),
                inventory.getTotalSeats(),
                inventory.getAvailableSeats()
        );
    }

    @Transactional
    public SeatInventory upsertInventory(Long eventId, Integer totalSeats, Integer availableSeats) {
        SeatInventory inventory = seatInventoryRepository.findById(eventId)
                .orElseGet(() -> {
                    SeatInventory inv = new SeatInventory();
                    inv.setEventId(eventId);
                    return inv;
                });

        Integer previousTotal = inventory.getTotalSeats();
        Integer previousAvailable = inventory.getAvailableSeats();

        if (totalSeats != null) {
            inventory.setTotalSeats(totalSeats);
        }

        if (availableSeats != null) {
            inventory.setAvailableSeats(availableSeats);
        } else if (totalSeats != null) {
            int baseAvailable = previousAvailable != null
                    ? previousAvailable
                    : (previousTotal != null ? previousTotal : totalSeats);
            int delta = totalSeats - (previousTotal != null ? previousTotal : 0);
            int newAvailable = Math.max(0, Math.min(totalSeats, baseAvailable + delta));
            inventory.setAvailableSeats(newAvailable);
        } else if (inventory.getAvailableSeats() == null && inventory.getTotalSeats() != null) {
            inventory.setAvailableSeats(inventory.getTotalSeats());
        }

        return seatInventoryRepository.save(inventory);
    }

    @Transactional
    public void adminDeleteTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        deleteTicketInternal(ticket);
    }

    private void deleteTicketInternal(Ticket ticket) {
        // if the ticket is holding seats, put them back before delete
        if (ticket.getStatus() == TicketStatus.LOCKED || ticket.getStatus() == TicketStatus.CONFIRMED) {
            restoreSeats(ticket);
        }
        ticketRepository.delete(ticket);
    }
}


