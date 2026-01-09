package com.eventverse.analyticsservice.controller;

import com.eventverse.analyticsservice.repository.view.DailyActiveUsersView;
import com.eventverse.analyticsservice.repository.view.DailyRevenueView;
import com.eventverse.analyticsservice.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsDashboardController {
    private final AnalyticsQueryService queryService;

    @GetMapping("/batch/daily-revenue")
    public List<DailyRevenueView> dailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String eventId
    ) {
        return queryService.dailyRevenue(startDate, endDate, eventId);
    }

    @GetMapping("/batch/dau")
    public List<DailyActiveUsersView> dailyActiveUsers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String eventId
    ) {
        return queryService.dailyActiveUsers(startDate, endDate, eventId);
    }

    @GetMapping("/dashboard/summary")
    public Map<String, Object> dashboard(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "5") int topEvents
    ) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);
        Map<String, Object> payload = new HashMap<>();
        payload.put("dailyRevenue", queryService.dailyRevenue(start, today, null));
        payload.put("dailyActiveUsers", queryService.dailyActiveUsers(start, today, null));
        payload.put("topEvents", queryService.topEvents(days, topEvents));
        return payload;
    }
}
