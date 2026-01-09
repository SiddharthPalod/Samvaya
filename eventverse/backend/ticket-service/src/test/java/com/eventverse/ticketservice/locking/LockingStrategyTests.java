package com.eventverse.ticketservice.locking;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext
class LockingStrategyTests {

    private static final long EVENT_ID = 42L;

    @Autowired
    private SeatInventoryRepository seatInventoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private TransactionTemplate committedTx;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        committedTx = new TransactionTemplate(transactionManager);
        committedTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        committedTx.execute(status -> {
            seatInventoryRepository.deleteAll();
            SeatInventory inventory = new SeatInventory();
            inventory.setEventId(EVENT_ID);
            inventory.setTotalSeats(100);
            inventory.setAvailableSeats(100);
            seatInventoryRepository.saveAndFlush(inventory);
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
    void optimisticLockingDetectsStaleWrites() {
        SeatInventory fresh = seatInventoryRepository.findById(EVENT_ID).orElseThrow();
        entityManager.detach(fresh);

        SeatInventory stale = seatInventoryRepository.findById(EVENT_ID).orElseThrow();
        entityManager.detach(stale);

        fresh.setAvailableSeats(90);
        seatInventoryRepository.saveAndFlush(fresh);

        stale.setAvailableSeats(80);
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> seatInventoryRepository.saveAndFlush(stale));
    }

    @Test
    void pessimisticLockingPreventsConcurrentAcquisition() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        CountDownLatch latch = new CountDownLatch(1);

        Future<?> lockHolder = executor.submit(() -> txTemplate.execute(status -> {
            seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
            latch.countDown();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }));

        latch.await(1, TimeUnit.SECONDS);

        Exception lockingFailure = assertThrows(Exception.class, () -> txTemplate.execute(status -> {
            seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
            return null;
        }));

        assertTrue(
                lockingFailure instanceof PessimisticLockingFailureException
                        || lockingFailure.getCause() instanceof PessimisticLockingFailureException,
                "Should fail fast when lock is already held");

        lockHolder.get(2, TimeUnit.SECONDS);
    }
}


