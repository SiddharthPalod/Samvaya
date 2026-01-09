package com.eventverse.analyticsservice.batch;
import com.eventverse.analyticsservice.repository.AnalyticsEventRepository;
import com.eventverse.analyticsservice.repository.view.DailyRevenueView;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRevenueJob {
    private static final Logger log = LoggerFactory.getLogger(DailyRevenueJob.class);
    private final AnalyticsEventRepository repository;

    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    public void computeDailyRevenue() {
        LocalDate targetDay = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate endDay = targetDay;
        List<DailyRevenueView> rows = repository.aggregateDailyRevenue(
                targetDay.atStartOfDay().toInstant(ZoneOffset.UTC),
                endDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusSeconds(1),
                null
        );
        rows.forEach(r -> log.info("Daily revenue {} = {}", r.getDay(), r.getRevenue()));
    }
}
