package com.eventverse.ticketservice.repository;
import com.eventverse.ticketservice.domain.Ticket;
import com.eventverse.ticketservice.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByIdempotencyKey(String idempotencyKey);
    Optional<Ticket> findByIdAndUserId(UUID id, Long userId);
    java.util.List<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId);
}
