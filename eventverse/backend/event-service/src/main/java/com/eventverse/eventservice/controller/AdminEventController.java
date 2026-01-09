package com.eventverse.eventservice.controller;

import com.eventverse.eventservice.dto.EventCreateRequest;
import com.eventverse.eventservice.dto.EventFilterRequest;
import com.eventverse.eventservice.dto.EventResponse;
import com.eventverse.eventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Super-admin endpoints for managing events without organizer checks.
 */
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventCreateRequest request) {
        return eventService.adminCreateEvent(request);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventCreateRequest request) {
        return eventService.adminUpdateEvent(id, request);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping
    public Page<EventResponse> list(EventFilterRequest filter) {
        return eventService.searchEvents(filter);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id, eventService.getEventById(id).getOrganizerId());
    }
}

