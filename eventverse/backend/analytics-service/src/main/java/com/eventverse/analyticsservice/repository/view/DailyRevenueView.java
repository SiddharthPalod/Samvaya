package com.eventverse.analyticsservice.repository.view;

import java.time.LocalDate;

public interface DailyRevenueView {
    LocalDate getDay();
    Double getRevenue();
}
