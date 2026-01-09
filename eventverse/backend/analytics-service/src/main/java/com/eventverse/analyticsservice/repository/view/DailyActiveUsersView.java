package com.eventverse.analyticsservice.repository.view;

import java.time.LocalDate;

public interface DailyActiveUsersView {
    LocalDate getDay();
    Long getDau();
}
