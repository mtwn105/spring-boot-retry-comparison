# Spring Framework retry demo

Baseline: Boot 4.0.8, Framework 7 via Boot dependency management. See the [root README](../README.md) for requirements, expected outcomes, scope and current validation status.

Activate with `@EnableResilientMethods`. The Framework `@Retryable` uses `maxRetries = 2`, meaning three total calls. No separate Spring Retry library is used.

Start with [InventoryService.java](src/main/java/dev/mtwn105/retry/frameworkretry/InventoryService.java), then [DemoApplication.java](src/main/java/dev/mtwn105/retry/frameworkretry/DemoApplication.java). The [integration test](src/test/java/dev/mtwn105/retry/frameworkretry/InventoryRetryTest.java) loads the real Boot application and inherits the shared retry contract.

From the repository root:

```bash
mvn -B -pl spring-framework-retry-demo -am verify
java -jar spring-framework-retry-demo/target/spring-framework-retry-demo.jar
```

The program runs all four failure scenarios and exits. It requires no server or external service. All seven tests in this module passed, and the packaged application passed all four console scenarios. See the [validation record](../VALIDATION.md).
