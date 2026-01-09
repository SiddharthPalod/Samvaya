package com.eventverse.ticketservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class EventPricingClient {
    public static final String EVENT_PRICING_CACHE = "eventPricing";

    private final RestTemplate restTemplate;
    private final String eventServiceBaseUrl;

    public EventPricingClient(RestTemplate restTemplate,
                              @Value("${event-service.base-url}") String eventServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }

    @org.springframework.cache.annotation.Cacheable(cacheNames = EVENT_PRICING_CACHE, key = "#eventId")
    public BigDecimal getPriceForEvent(Long eventId) {
        String url = eventServiceBaseUrl + "/internal/events/" + eventId + "/pricing";
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        Object price = response.get("price");
        return new BigDecimal(price.toString());
    }
}

