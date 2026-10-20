package dev.mtwn105.retry.springretry;

import dev.mtwn105.retry.common.InventoryProbe;
import dev.mtwn105.retry.common.TransientInventoryException;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
public class InventoryService {
    @Retryable(retryFor = TransientInventoryException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 1000))
    public String lookup(InventoryProbe probe) {
        return probe.fetch();
    }

    // Calls inside this object bypass Spring's retry proxy.
    public String lookupViaSelfInvocation(InventoryProbe probe) {
        return lookup(probe);
    }
}
