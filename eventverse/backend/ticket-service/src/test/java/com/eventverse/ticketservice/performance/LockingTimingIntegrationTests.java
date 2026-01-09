package com.eventverse.ticketservice.performance;

import com.eventverse.ticketservice.domain.SeatInventory;
import com.eventverse.ticketservice.repository.SeatInventoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LockingTimingIntegrationTests {

    private static final long EVENT_ID = 99L;

    @Autowired
    private SeatInventoryRepository seatInventoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private TransactionTemplate txRequiresNew;
    private ExecutorService executor;

    @BeforeEach
    void setup() {
        txRequiresNew = new TransactionTemplate(transactionManager);
        txRequiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        txRequiresNew.execute(status -> {
            seatInventoryRepository.deleteAll();
            SeatInventory inv = new SeatInventory();
            inv.setEventId(EVENT_ID);
            inv.setTotalSeats(100);
            inv.setAvailableSeats(100);
            seatInventoryRepository.saveAndFlush(inv);
            return null;
        });
        entityManager.clear();
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void pessimisticPathTakesLongerUnderLockHoldThanOptimisticConflict() throws Exception {
        long optimisticNanos = measureOptimisticConflict();
        long pessimisticNanos = measurePessimisticContention();

        long padding = Duration.ofMillis(75).toNanos(); // allow slow hosts
        assertTrue(pessimisticNanos > optimisticNanos + padding,
                () -> "Expected pessimistic path to be slower. optimistic=" + optimisticNanos
                        + "ns, pessimistic=" + pessimisticNanos + "ns");
    }

    private long measureOptimisticConflict() {
        return time(() -> {
            SeatInventory fresh = seatInventoryRepository.findById(EVENT_ID).orElseThrow();
            entityManager.detach(fresh);

            SeatInventory stale = seatInventoryRepository.findById(EVENT_ID).orElseThrow();
            entityManager.detach(stale);

            fresh.setAvailableSeats(90);
            seatInventoryRepository.saveAndFlush(fresh);

            stale.setAvailableSeats(80);
            try {
                seatInventoryRepository.saveAndFlush(stale);
            } catch (ObjectOptimisticLockingFailureException expected) {
                // intentionally ignored for timing
            }
        });
    }

    private long measurePessimisticContention() throws Exception {
        CountDownLatch lockAcquired = new CountDownLatch(1);

        Future<?> holder = executor.submit(() -> txRequiresNew.execute(status -> {
            seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
            lockAcquired.countDown();
            try {
                Thread.sleep(150); // simulate a longer critical section
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }));

        lockAcquired.await(1, TimeUnit.SECONDS);

        long duration = time(() -> txRequiresNew.execute(status -> {
            try {
                seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
            } catch (PessimisticLockingFailureException expected) {
                // expected under contention
            }
            return null;
        }));

        holder.get(2, TimeUnit.SECONDS);
        return duration;
    }

    private long time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return System.nanoTime() - start;
    }
}


