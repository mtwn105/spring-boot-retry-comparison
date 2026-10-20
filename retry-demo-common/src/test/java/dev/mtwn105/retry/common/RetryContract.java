package dev.mtwn105.retry.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Inherited by each adapter test: the same behavioral contract for all implementations. */
public abstract class RetryContract {
    protected abstract InventoryLookup lookup();

    @Test
    void immediateSuccessMakesOneCall() {
        var probe = InventoryProbe.failingThenSucceeding(0);
        assertEquals("IN_STOCK", lookup().lookup(probe));
        assertEquals(1, probe.attempts());
    }

    @Test
    void twoTransientFailuresRecoverOnThirdCall() {
        var probe = InventoryProbe.failingThenSucceeding(2);
        assertEquals("IN_STOCK", lookup().lookup(probe));
        assertEquals(3, probe.attempts());
    }

    @Test
    void exhaustionStopsAtThreeAndPropagatesLastOriginalFailure() {
        var probe = InventoryProbe.failingThenSucceeding(100);
        var failure = assertThrows(TransientInventoryException.class, () -> lookup().lookup(probe));
        assertEquals(3, probe.attempts());
        assertSame(probe.lastFailure(), failure);
    }

    @Test
    void permanentFailureIsNeverRetried() {
        var probe = InventoryProbe.permanentlyFailing();
        var failure = assertThrows(PermanentInventoryException.class, () -> lookup().lookup(probe));
        assertEquals(1, probe.attempts());
        assertSame(probe.lastFailure(), failure);
    }

    @Test
    void retryStateDoesNotLeakAcrossLogicalOperations() {
        var first = InventoryProbe.failingThenSucceeding(2);
        var second = InventoryProbe.failingThenSucceeding(2);
        assertEquals("IN_STOCK", lookup().lookup(first));
        assertEquals("IN_STOCK", lookup().lookup(second));
        assertEquals(3, first.attempts());
        assertEquals(3, second.attempts());
    }

    @Test
    void backoffSeparatesCallsAndGrowsExponentially() {
        var probe = InventoryProbe.failingThenSucceeding(2);
        assertEquals("IN_STOCK", lookup().lookup(probe));
        var times = probe.attemptNanos();
        assertEquals(3, times.size());
        // 100ms then 200ms; allow 10ms clock/scheduler tolerance. No flaky upper bound.
        assertTrue(times.get(1) - times.get(0) >= 90_000_000L, "First backoff too short");
        assertTrue(times.get(2) - times.get(1) >= 190_000_000L, "Second backoff too short");
    }
}
