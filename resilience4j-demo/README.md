# Resilience4j demo

Baseline: Boot 3.5.16, Resilience4j 2.3.0 Boot 3 starter. See the [root README](../README.md) for requirements, expected outcomes, scope and current validation status.

The `@Retry(name = "inventory")` annotation refers to the named policy in [application.yml](src/main/resources/application.yml). The Boot starter configures the aspect; `spring-boot-starter-aop` and Actuator are explicit dependencies. No fallback method is configured.

Start with [InventoryService.java](src/main/java/dev/mtwn105/retry/resilience4j/InventoryService.java), then [DemoApplication.java](src/main/java/dev/mtwn105/retry/resilience4j/DemoApplication.java). The [integration test](src/test/java/dev/mtwn105/retry/resilience4j/InventoryRetryTest.java) loads the real Boot application and inherits the shared retry contract.

From the repository root:

```bash
mvn -B -pl resilience4j-demo -am verify
java -jar resilience4j-demo/target/resilience4j-demo.jar
```

The program runs all four failure scenarios and exits. It requires no server or external service. All seven tests in this module passed, and the packaged application passed all four console scenarios. See the [validation record](../VALIDATION.md).
