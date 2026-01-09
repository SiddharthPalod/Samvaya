package com.eventverse.eventservice.repository;

import com.eventverse.eventservice.domain.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByPublicEventTrueOrderByPopularityScoreDescTimeAsc(Pageable pageable);

    List<Event> findByCityAndPublicEventTrueOrderByPopularityScoreDescTimeAsc(String city, Pageable pageable);

    List<Event> findByPublicEventTrueAndTimeAfterOrderByTimeAsc(OffsetDateTime now, Pageable pageable);

    List<Event> findByCityAndPublicEventTrueAndTimeAfterOrderByTimeAsc(String city, OffsetDateTime now, Pageable pageable);
}