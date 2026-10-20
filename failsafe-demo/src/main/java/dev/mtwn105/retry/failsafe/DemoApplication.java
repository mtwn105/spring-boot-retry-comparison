package dev.mtwn105.retry.failsafe;

import dev.mtwn105.retry.common.DemoSuite;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;


@SpringBootApplication

public class DemoApplication {
    public static void main(String[] args) {
        try (var context = SpringApplication.run(DemoApplication.class, args)) {
            // Console application: close the context once the scenarios finish.
        }
    }

    @Bean
    @ConditionalOnProperty(name = "demo.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner scenarios(InventoryService service) {
        return args -> DemoSuite.run("Failsafe", service::lookup);
    }
}
