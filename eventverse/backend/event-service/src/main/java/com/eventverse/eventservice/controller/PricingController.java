package com.eventverse.eventservice.controller;

import com.eventverse.eventservice.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/internal/events")
public class PricingController {

    private final EventService eventService;

    public PricingController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{id}/pricing")
    public Map<String, Object> getPricing(@PathVariable Long id) {
        BigDecimal price = eventService.getPriceForEvent(id);
        return Map.of("price", price);
    }
}

