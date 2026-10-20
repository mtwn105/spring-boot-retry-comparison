package dev.mtwn105.retry.failsafe;

import dev.mtwn105.retry.common.InventoryProbe;
import dev.mtwn105.retry.common.TransientInventoryException;
import org.springframework.stereotype.Service;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.time.temporal.ChronoUnit;

@Service
public class InventoryService {
    private final RetryPolicy<String> policy = RetryPolicy.<String>builder()
            .handle(TransientInventoryException.class)
            .withMaxAttempts(3)
            .withBackoff(100, 1000, ChronoUnit.MILLIS, 2.0)
            .build();

    public String lookup(InventoryProbe probe) {
        return Failsafe.with(policy).get(probe::fetch);
    }

    // Retry is explicit inside lookup, so self-invocation still applies the policy.
    public String lookupViaSelfInvocation(InventoryProbe probe) {
        return lookup(probe);
    }
}
