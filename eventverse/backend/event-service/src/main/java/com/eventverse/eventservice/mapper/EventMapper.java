package com.eventverse.eventservice.mapper;
import com.eventverse.eventservice.domain.Event;
import com.eventverse.eventservice.dto.EventCreateRequest;
import com.eventverse.eventservice.dto.EventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public Event toEntity(EventCreateRequest req) {
        Event e = new Event();
        e.setTitle(req.getTitle());
        e.setDescription(req.getDescription());
        e.setCity(req.getCity());
        e.setTime(req.getTime());
        e.setCapacity(req.getCapacity());
        e.setOrganizerId(req.getOrganizerId());
        e.setVenue(req.getVenue());
        e.setCategory(req.getCategory());
        e.setImageUrl(req.getImageUrl());
        e.setPublicEvent(req.getPublicEvent() != null ? req.getPublicEvent() : true);
        if (req.getPrice() != null) {
            e.setPrice(req.getPrice());
        }
        return e;
    }

    public EventResponse toResponse(Event e) {
        EventResponse res = new EventResponse();
        res.setId(e.getId());
        res.setTitle(e.getTitle());
        res.setDescription(e.getDescription());
        res.setCity(e.getCity());
        res.setTime(e.getTime());
        res.setCapacity(e.getCapacity());
        res.setTotalSeats(e.getCapacity());
        res.setOrganizerId(e.getOrganizerId());
        res.setVenue(e.getVenue());
        res.setCategory(e.getCategory());
        res.setImageUrl(e.getImageUrl());
        res.setPublicEvent(e.isPublicEvent());
        res.setPopularityScore(e.getPopularityScore());
        res.setPrice(e.getPrice());

        return res;
    }
}
