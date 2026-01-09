package com.eventverse.analyticsservice.repository;
import com.eventverse.analyticsservice.entity.AnalyticsEvent;
import com.eventverse.analyticsservice.repository.view.DailyActiveUsersView;
import com.eventverse.analyticsservice.repository.view.DailyRevenueView;
import com.eventverse.analyticsservice.repository.view.TopEventView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    @Query(
            value = """
                SELECT date(to_timestamp(a.timestamp / 1000)) AS day,
                       SUM(a.value)                         AS revenue
                FROM analytics_events a
                WHERE a.event_type = 'TICKET_PURCHASED'
                  AND (:eventId IS NULL OR a.entity_id = CAST(:eventId AS BIGINT))
                  AND to_timestamp(a.timestamp / 1000) BETWEEN :startTs AND :endTs
                GROUP BY day
                ORDER BY day
            """,
            nativeQuery = true
    )
    List<DailyRevenueView> aggregateDailyRevenue(@Param("startTs") Instant start, @Param("endTs") Instant end, @Param("eventId") String eventId);

    @Query(
            value = """
                SELECT date(to_timestamp(a.timestamp / 1000)) AS day,
                       COUNT(DISTINCT a.user_id)             AS dau
                FROM analytics_events a
                WHERE (:eventId IS NULL OR a.entity_id = CAST(:eventId AS BIGINT))
                  AND to_timestamp(a.timestamp / 1000) BETWEEN :startTs AND :endTs
                GROUP BY day
                ORDER BY day
            """,
            nativeQuery = true
    )
    List<DailyActiveUsersView> aggregateDailyActiveUsers(@Param("startTs") Instant start, @Param("endTs") Instant end, @Param("eventId") String eventId);

    @Query(
            value = """
                SELECT a.entity_id                  AS eventId,
                       SUM(a.value)                 AS revenue,
                       COUNT(*)                     AS tickets
                FROM analytics_events a
                WHERE a.event_type = 'TICKET_PURCHASED'
                  AND to_timestamp(a.timestamp / 1000) >= :sinceTs
                GROUP BY a.entity_id
                ORDER BY revenue DESC
                LIMIT :limit
            """,
            nativeQuery = true
    )
    List<TopEventView> topEventsSince(@Param("sinceTs") Instant since, @Param("limit") int limit);
}
