package dev.mtwn105.retry.springretry;

import dev.mtwn105.retry.common.DemoSuite;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry(proxyTargetClass = true)
public class DemoApplication {
    public static void main(String[] args) {
        try (var context = SpringApplication.run(DemoApplication.class, args)) {
            // Console application: close the context once the scenarios finish.
        }
    }

    @Bean
    @ConditionalOnProperty(name = "demo.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner scenarios(InventoryService service) {
        return args -> DemoSuite.run("Spring Retry", service::lookup);
    }
}
