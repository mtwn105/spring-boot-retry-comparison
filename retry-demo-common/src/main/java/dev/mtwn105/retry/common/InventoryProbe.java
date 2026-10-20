package dev.mtwn105.retry.common;

import java.util.ArrayList;
import java.util.List;

/** A deterministic in-process dependency. One probe per sequential operation; not thread-safe. */
public final class InventoryProbe {
    private final int transientFailures;
    private final boolean permanentFailure;
    private final List<Long> attemptNanos = new ArrayList<>();
    private RuntimeException lastFailure;

    private InventoryProbe(int transientFailures, boolean permanentFailure) {
        if (transientFailures < 0) {
            throw new IllegalArgumentException("transientFailures must be nonnegative");
        }
        this.transientFailures = transientFailures;
        this.permanentFailure = permanentFailure;
    }

    public static InventoryProbe failingThenSucceeding(int failures) {
        return new InventoryProbe(failures, false);
    }

    public static InventoryProbe permanentlyFailing() {
        return new InventoryProbe(0, true);
    }

    public String fetch() {
        attemptNanos.add(System.nanoTime());
        if (permanentFailure) {
            lastFailure = new PermanentInventoryException("Invalid product identifier");
            throw lastFailure;
        }
        if (attempts() <= transientFailures) {
            lastFailure = new TransientInventoryException("Inventory unavailable on attempt " + attempts());
            throw lastFailure;
        }
        return "IN_STOCK";
    }

    public int attempts() {
        return attemptNanos.size();
    }

    public List<Long> attemptNanos() {
        return List.copyOf(attemptNanos);
    }

    public RuntimeException lastFailure() {
        return lastFailure;
    }
}
