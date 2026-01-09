package com.eventverse.eventservice.service;

import com.eventverse.eventservice.dto.SeatAvailabilityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TicketInventoryClient {
    private final RestTemplate restTemplate;
    private final String ticketServiceBaseUrl;

    public TicketInventoryClient(RestTemplate restTemplate,
                                 @Value("${ticket-service.base-url}") String ticketServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.ticketServiceBaseUrl = ticketServiceBaseUrl;
    }

    public SeatAvailabilityResponse fetchAvailability(Long eventId) {
        String url = ticketServiceBaseUrl + "/tickets/" + eventId + "/availability";
        return restTemplate.getForObject(url, SeatAvailabilityResponse.class);
    }

    public SeatAvailabilityResponse syncCapacity(Long eventId, Integer totalSeats) {
        if (totalSeats == null) {
            return null;
        }
        String url = ticketServiceBaseUrl + "/admin/inventory/" + eventId;
        restTemplate.put(url, Map.of("totalSeats", totalSeats));
        return fetchAvailability(eventId);
    }
}
