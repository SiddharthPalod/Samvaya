package com.eventverse.eventservice.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
@Component
public class EventPricingClient {
    private final RestTemplate restTemplate;
    private final String eventServiceBaseUrl;

    public EventPricingClient(RestTemplate restTemplate,
                              @Value("${event-service.base-url}") String eventServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }
    public BigDecimal getPriceForEvent(Long eventId) {
        String url = eventServiceBaseUrl + "/internal/events/" + eventId + "/pricing";
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        Object price = response.get("price");
        return new BigDecimal(price.toString());
    }
}
