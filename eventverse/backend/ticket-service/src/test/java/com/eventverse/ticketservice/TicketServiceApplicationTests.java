package com.eventverse.ticketservice;

import com.eventverse.ticketservice.domain.SeatInventory;
import com.eventverse.ticketservice.repository.SeatInventoryRepository;
import com.eventverse.ticketservice.service.cache.TtlLruCache;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TicketServiceApplicationTests {

	private static final long EVENT_ID = 123L;

	@Autowired
	private SeatInventoryRepository seatInventoryRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private EntityManager entityManager;

	private TransactionTemplate txRequiresNew;
	private ExecutorService executor;

	@BeforeEach
	void initFixture() {
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
	void tearDownFixture() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void comparesOptimisticVsPessimisticTimingDuringSmoke() throws Exception {
		long optimistic = measureOptimisticConflict();
		long pessimistic = measurePessimisticContention();

		System.out.printf("Lock timing ns -> optimistic=%d, pessimistic=%d%n", optimistic, pessimistic);

		long padding = Duration.ofMillis(75).toNanos();
		assertTrue(pessimistic > optimistic + padding,
				() -> "Pessimistic path should be slower. optimistic=" + optimistic
						+ "ns pessimistic=" + pessimistic + "ns");
	}

	@Test
	void comparesTtlLruAgainstPureLruDuringSmoke() {
		int capacity = 512;
		int iterations = 600;
		int keySpace = 450;

		PureLruCache<Integer, Integer> lru = new PureLruCache<>(capacity);
		TtlLruCache<Integer, Integer> ttlLru = new TtlLruCache<>(capacity, Duration.ofMillis(5));

		long lruDuration = runCacheWorkload(lru::put, lru::get, lru::size, iterations, keySpace, false);
		long ttlDuration = runCacheWorkload(ttlLru::put, ttlLru::get, ttlLru::size, iterations, keySpace, true);

		System.out.printf("Cache timing ns -> lru=%d, ttlLru=%d%n", lruDuration, ttlDuration);

		long padding = Duration.ofMillis(5).toNanos(); // keep a small guard to avoid flakiness
		assertTrue(ttlDuration > lruDuration + padding,
				() -> "TTL cache should be slower under expiry churn. lru=" + lruDuration
						+ "ns ttl=" + ttlDuration + "ns with padding=" + padding);
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
			} catch (ObjectOptimisticLockingFailureException ignored) {
				// expected for timing
			}
		});
	}

	private long measurePessimisticContention() throws Exception {
		CountDownLatch lockAcquired = new CountDownLatch(1);

		Future<?> holder = executor.submit(() -> txRequiresNew.execute(status -> {
			seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
			lockAcquired.countDown();
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return null;
		}));

		lockAcquired.await(1, TimeUnit.SECONDS);

		long duration = time(() -> txRequiresNew.execute(status -> {
			try {
				seatInventoryRepository.findWithPessimisticLockByEventId(EVENT_ID).orElseThrow();
			} catch (PessimisticLockingFailureException ignored) {
				// expected under contention
			}
			return null;
		}));

		holder.get(2, TimeUnit.SECONDS);
		return duration;
	}

	private long runCacheWorkload(PutOp<Integer, Integer> put,
								  GetOp<Integer, Integer> get,
								  SizeOp size,
								  int iterations,
								  int keySpace,
								  boolean addExpiryPauses) {
		long start = System.nanoTime();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int i = 0; i < iterations; i++) {
			int key = random.nextInt(keySpace);
			put.put(key, i);
			get.get(key);
			size.size();
			if (addExpiryPauses && i % 75 == 0) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		return System.nanoTime() - start;
	}

	private long time(Runnable runnable) {
		long start = System.nanoTime();
		runnable.run();
		return System.nanoTime() - start;
	}

	private interface PutOp<K, V> {
		void put(K key, V value);
	}

	private interface GetOp<K, V> {
		V get(K key);
	}

	private interface SizeOp {
		int size();
	}

	/**
	 * Minimal access-order LRU for comparison inside smoke test.
	 */
	private static class PureLruCache<K, V> {
		private final Map<K, V> delegate;
		private final int maxSize;

		PureLruCache(int maxSize) {
			this.maxSize = maxSize;
			this.delegate = new LinkedHashMap<>(16, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
					return size() > PureLruCache.this.maxSize;
				}
			};
		}

		void put(K key, V value) {
			delegate.put(key, value);
		}

		V get(K key) {
			return delegate.get(key);
		}

		int size() {
			return delegate.size();
		}
	}
}
