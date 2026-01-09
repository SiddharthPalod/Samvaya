package com.eventverse.eventservice.controller;
import com.eventverse.eventservice.dto.EventCreateRequest;
import com.eventverse.eventservice.dto.EventFilterRequest;
import com.eventverse.eventservice.dto.EventResponse;
import com.eventverse.eventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        // trust organizer from gateway JWT
        request.setOrganizerId(userId);
        return eventService.createEvent(request);
    }


    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/search")
    public Page<EventResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return eventService.searchFullText(query, city, page, size);
    }


    @PutMapping("/{id}")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return eventService.updateEvent(id, request, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        eventService.deleteEvent(id, userId);
    }


    @GetMapping
    public Page<EventResponse> listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long organizerId,
            @RequestParam(required = false) String fromTime,
            @RequestParam(required = false) String toTime,
            @RequestParam(defaultValue = "TIME_ASC") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        EventFilterRequest filter = new EventFilterRequest();
        filter.setCity(city);
        filter.setOrganizerId(organizerId);
        filter.setSort(sort);
        filter.setPage(page);
        filter.setSize(size);

        if (fromTime != null) {
            filter.setFromTime(OffsetDateTime.parse(fromTime));
        }
        if (toTime != null) {
            filter.setToTime(OffsetDateTime.parse(toTime));
        }

        return eventService.searchEvents(filter);
    }
}
