# Failsafe demo

Baseline: Boot 3.5.16, Failsafe 3.3.2. See the [root README](../README.md) for requirements, expected outcomes, scope and current validation status.

The policy is explicit: `Failsafe.with(policy).get(probe::fetch)`. Spring supplies the service bean, but retry itself does not depend on annotation advice. Self-invocation therefore still executes the policy.

Start with [InventoryService.java](src/main/java/dev/mtwn105/retry/failsafe/InventoryService.java), then [DemoApplication.java](src/main/java/dev/mtwn105/retry/failsafe/DemoApplication.java). The [integration test](src/test/java/dev/mtwn105/retry/failsafe/InventoryRetryTest.java) loads the real Boot application and inherits the shared retry contract.

From the repository root:

```bash
mvn -B -pl failsafe-demo -am verify
java -jar failsafe-demo/target/failsafe-demo.jar
```

The program runs all four failure scenarios and exits. It requires no server or external service. All seven tests in this module passed, and the packaged application passed all four console scenarios. See the [validation record](../VALIDATION.md).
