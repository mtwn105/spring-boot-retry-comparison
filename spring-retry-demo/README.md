# Spring Retry demo

Baseline: Boot 3.5.16, Spring Retry 2.0.13. See the [root README](../README.md) for requirements, expected outcomes, scope and current validation status.

Activate with `@EnableRetry`. The legacy `@Retryable` uses `maxAttempts = 3`, including the first call. No `@Recover` method is configured; exhaustion propagates the failure.

Start with [InventoryService.java](src/main/java/dev/mtwn105/retry/springretry/InventoryService.java), then [DemoApplication.java](src/main/java/dev/mtwn105/retry/springretry/DemoApplication.java). The [integration test](src/test/java/dev/mtwn105/retry/springretry/InventoryRetryTest.java) loads the real Boot application and inherits the shared retry contract.

From the repository root:

```bash
mvn -B -pl spring-retry-demo -am verify
java -jar spring-retry-demo/target/spring-retry-demo.jar
```

The program runs all four failure scenarios and exits. It requires no server or external service. All seven tests in this module passed, and the packaged application passed all four console scenarios. See the [validation record](../VALIDATION.md).
