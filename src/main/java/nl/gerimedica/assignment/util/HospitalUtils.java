package nl.gerimedica.assignment.util;

import java.util.concurrent.atomic.AtomicLong;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe utility class to record usage events in the application.
 * Uses AtomicLong for lock-free counters and logs context for observability.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HospitalUtils
{

    private static final AtomicLong USAGE_COUNTER = new AtomicLong(0);

    /**
     * Increments usage counter and logs the context.
     */
    public static void recordUsage(String context)
    {
        long current = USAGE_COUNTER.incrementAndGet();
        log.info("HospitalUtils used. Counter: {} | Context: {}", current, context);
    }

    public static long getUsageCount()
    {
        return USAGE_COUNTER.get();
    }
}
