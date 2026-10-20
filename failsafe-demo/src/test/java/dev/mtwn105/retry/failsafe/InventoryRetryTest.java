package dev.mtwn105.retry.failsafe;

import dev.mtwn105.retry.common.InventoryLookup;
import dev.mtwn105.retry.common.InventoryProbe;
import dev.mtwn105.retry.common.RetryContract;
import dev.mtwn105.retry.common.TransientInventoryException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryRetryTest extends RetryContract {
    private static ConfigurableApplicationContext context;

    @BeforeAll
    static void startApplication() {
        // Load the real Boot configuration, including YAML and retry auto-configuration.
        context = new SpringApplicationBuilder(DemoApplication.class)
                .web(WebApplicationType.NONE)
                .run("--demo.enabled=false");
    }

    @AfterAll
    static void closeApplication() {
        if (context != null) {
            context.close();
        }
    }

    private InventoryService service() {
        return context.getBean(InventoryService.class);
    }

    @Override
    protected InventoryLookup lookup() {
        return service()::lookup;
    }

    @Test
    void explicitPolicyStillRunsOnSelfInvocation() {
        var probe = InventoryProbe.failingThenSucceeding(2);
        assertEquals("IN_STOCK", service().lookupViaSelfInvocation(probe));
        assertEquals(3, probe.attempts());
    }
}
