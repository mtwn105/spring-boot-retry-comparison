package dev.mtwn105.retry.common;

import java.util.Locale;
import java.util.stream.Collectors;

/** Exercises the real adapter; a broken retry policy makes the process fail. */
public final class DemoSuite {
    private DemoSuite() {}

    public static void run(String implementation, InventoryLookup lookup) {
        System.out.println("\nImplementation: " + implementation);
        scenario("immediate-success", InventoryProbe.failingThenSucceeding(0), lookup, 1, null);
        scenario("fail-twice-then-success", InventoryProbe.failingThenSucceeding(2), lookup, 3, null);
        scenario("exhausted", InventoryProbe.failingThenSucceeding(100), lookup,
                3, TransientInventoryException.class);
        scenario("permanent-error", InventoryProbe.permanentlyFailing(), lookup,
                1, PermanentInventoryException.class);
        System.out.println("All four scenario contracts passed.");
    }

    private static void scenario(String name, InventoryProbe probe, InventoryLookup lookup,
                                 int expectedAttempts, Class<? extends RuntimeException> expectedError) {
        String result = null;
        RuntimeException failure = null;
        try {
            result = lookup.lookup(probe);
        } catch (RuntimeException ex) {
            failure = ex;
        }
        boolean outcomeMatches = expectedError == null
                ? failure == null && "IN_STOCK".equals(result)
                : failure != null && expectedError.isInstance(failure) && failure == probe.lastFailure();
        if (probe.attempts() != expectedAttempts || !outcomeMatches) {
            throw new IllegalStateException("Scenario contract failed: " + name
                    + ", attempts=" + probe.attempts() + ", result=" + result, failure);
        }
        long first = probe.attemptNanos().get(0);
        String offsets = probe.attemptNanos().stream()
                .map(nanos -> String.format(Locale.ROOT, "%.1f", (nanos - first) / 1_000_000.0))
                .collect(Collectors.joining(", "));
        System.out.printf("%s attempts=%d outcome=%s offsetsMs=[%s]%n", name, probe.attempts(),
                failure == null ? result : failure.getClass().getSimpleName(), offsets);
    }
}
