package dev.mtwn105.retry.resilience4j;

import dev.mtwn105.retry.common.InventoryProbe;
import dev.mtwn105.retry.common.TransientInventoryException;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class InventoryService {
    // The named policy lives in application.yml and is bound by the Boot 3 starter.
    @Retry(name = "inventory")
    public String lookup(InventoryProbe probe) {
        return probe.fetch();
    }

    // Calls inside this object bypass the retry aspect.
    public String lookupViaSelfInvocation(InventoryProbe probe) {
        return lookup(probe);
    }
}
