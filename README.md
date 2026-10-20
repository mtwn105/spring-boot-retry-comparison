# Spring Boot retry comparison

One Maven repository with four implementations of the same retry policy:

- **Spring Retry:** annotation-based retry in an existing Spring Boot 3 application.
- **Spring Framework retry:** the built-in Framework 7 annotation in a Boot 4 application.
- **Resilience4j:** annotation-based retry configured through its Boot 3 starter and YAML.
- **Failsafe:** an explicit programmatic policy inside a Spring Boot application.

The simulated dependency is an in-process inventory lookup. No Docker, database, external API, credentials, or running server is needed. This isolates retry behavior from networking. It does not demonstrate HTTP status handling, connection/read timeouts, idempotency, distributed state, or throughput.

**Validation status:** all **28 integration tests passed** (7 per implementation, no failures, errors or skipped tests). All four packaged applications also passed their four console scenarios. Results come from inspected Maven Surefire reports and the author-provided `run-all.sh` output. See [VALIDATION.md](VALIDATION.md) and the sanitized evidence in [validation/](validation/).

## Requirements and pinned versions

Use **JDK 21** and **Maven 3.9+**. Sources target Java 17 bytecode; Java 17 execution has not been tested. The first build requires network access to Maven Central. Maven itself is a prerequisite; no Maven wrapper is included.

| Module | Boot baseline | Retry dependency / API |
| --- | --- | --- |
| `spring-retry-demo` | 3.5.16 | `spring-retry:2.0.13` |
| `spring-framework-retry-demo` | 4.0.8 | Framework 7 resilience supplied through Boot |
| `resilience4j-demo` | 3.5.16 | `resilience4j-spring-boot3:2.3.0` |
| `failsafe-demo` | 3.5.16 | `failsafe:3.3.2` |

Versions are pinned comparison baselines, not a claim that each is the newest release. Boot dependency management is imported separately by each application so the Framework 6 and 7 dependency graphs stay isolated. A shared JUnit 5.12.2 contract runs against the actual application context in each module. Boot's test starter is not used.

Spring Retry is archived; it is included to explain existing applications and migration decisions. See the [maintainer repository](https://github.com/spring-attic/spring-retry). Framework resilience belongs to [Spring Framework 7](https://docs.spring.io/spring-framework/reference/core/resilience.html), and needs explicit activation in this sample. It is not an automatically enabled retry policy for every Spring Boot HTTP call. The Resilience4j example deliberately uses its documented Boot 3 starter; no Boot 4 starter compatibility is claimed.

## Build and run

Run these commands **from this directory**, which becomes the root of the standalone repository:

```bash
java -version
mvn -version
mvn -B verify
```

`verify` compiles and packages every module and runs the integration tests. After a successful build, run any example independently:

```bash
java -jar spring-retry-demo/target/spring-retry-demo.jar
java -jar spring-framework-retry-demo/target/spring-framework-retry-demo.jar
java -jar resilience4j-demo/target/resilience4j-demo.jar
java -jar failsafe-demo/target/failsafe-demo.jar
```

Or run all four with:

```bash
bash scripts/run-all.sh
```

Each console application runs four scenarios, checks the outcome and attempt count, prints invocation offsets, then closes its application context. An unexpected result causes a failing process exit. Nothing listens on a port.

To build just one implementation and its shared dependency:

```bash
mvn -B -pl spring-framework-retry-demo -am verify
```

`-am` includes the shared module. To work directly inside a module afterward, first run `mvn install` from the root so the parent, shared jar and shared test jar are available in the local Maven repository. Executable application jars are self-contained once packaged.

## Equivalent policy, different APIs

All four examples implement:

- At most **three total invocations** per logical operation.
- Retry only `TransientInventoryException`.
- Exponential backoff: 100ms before call two, 200ms before call three; configured cap 1000ms.
- No jitter, fallback, circuit breaker, asynchronous execution, or nested retry layer.
- Propagate the final original exception when attempts are exhausted.

Jitter is omitted to make the policy comparison observable and repeatable. With only two retry delays, the 1000ms cap is configured but never reached by these scenarios; this example does not test cap saturation.

| Implementation | Attempt limit | Policy source |
| --- | --- | --- |
| Spring Retry | `maxAttempts = 3` | `InventoryService.java` |
| Framework retry | `maxRetries = 2` | `InventoryService.java` |
| Resilience4j | `max-attempts: 3` | `application.yml` |
| Failsafe | `.withMaxAttempts(3)` | `InventoryService.java` |

Framework's retry count excludes the initial invocation. Explicitly converting the limit avoids an off-by-one comparison. The annotation imports differ too: legacy `org.springframework.retry.annotation.Retryable`, Framework `org.springframework.resilience.annotation.Retryable`, and Resilience4j `io.github.resilience4j.retry.annotation.Retry`.

## Scenarios and expected results

These contracts passed in all four implementations. The [captured console output](validation/run-all.txt) includes observed invocation offsets:

| Scenario | Dependency behavior | Expected invocations | Expected outcome |
| --- | --- | --- | --- |
| `immediate-success` | Succeeds immediately | 1 | `IN_STOCK` |
| `fail-twice-then-success` | Two transient failures, then success | 3 | `IN_STOCK` |
| `exhausted` | More failures than the policy permits | 3 | `TransientInventoryException` |
| `permanent-error` | Invalid product identifier | 1 | `PermanentInventoryException` |

Offsets are measured using `System.nanoTime()` and displayed relative to the first invocation. They include scheduler and execution overhead. They are not library performance benchmarks.

## Project map

```text
pom.xml
retry-demo-common/             # Probe, exceptions, console scenarios, shared test contract
spring-retry-demo/             # @EnableRetry + legacy @Retryable
spring-framework-retry-demo/   # @EnableResilientMethods + Framework @Retryable
resilience4j-demo/             # @Retry + Boot starter + application.yml
failsafe-demo/                 # RetryPolicy + Failsafe.with(...).get(...)
scripts/run-all.sh
VALIDATION.md
```

Read each `InventoryService.java` first, then its `DemoApplication.java`; for Resilience4j also read `application.yml`. Each module has a short README with direct source links.

## What the tests verify

Each adapter inherits six identical tests from `RetryContract`: immediate success, eventual success, exhaustion with original exception identity, permanent-error exclusion, independent state across operations, and minimum backoff intervals. Each module adds a seventh test for self-invocation.

For the three annotation-based examples, an external Spring bean invocation is required for retry advice. `lookupViaSelfInvocation()` calls the annotated method inside the same object and bypassed that advice in the integration tests. The Failsafe policy executes explicitly inside the method and remained active on self-invocation in the integration tests. Tests load the real Boot context and YAML configuration, so they exercise wiring as well as policy behavior.

The timing test checks lower bounds with 10ms tolerance and no upper bound; it detects omitted or fixed 100ms backoff, but is not a precise timing guarantee. The tests do not establish recovery/fallback behavior, transaction interaction, nested exception matching, circuit breaker ordering, metrics export, reactive execution, or cancellation of in-flight I/O.

## Troubleshooting and cleanup

- **Unknown host / transfer failure:** restore Maven Central access, then run `mvn -U verify` to retry cached transfer failures. Missing managed versions can be secondary errors when a BOM could not be downloaded.
- **Unsupported class-file / invalid release:** check that both `java -version` and `mvn -version` use the intended JDK.
- **No shared artifact when building a module:** use root `-pl <module> -am verify` or install the reactor first.
- **One invocation instead of three:** check the annotation import, activation, proxy call path, and (for Resilience4j) the named YAML policy. Do not instantiate an annotated service with `new` and expect Spring advice.
- **No executable jar:** finish `mvn verify`; plain `mvn test` does not package applications.

Applications exit on their own. `mvn clean` removes generated module build directories. These demos create no infrastructure or external resources.

## References

- [Spring Retry source and status](https://github.com/spring-attic/spring-retry)
- [Framework retry annotation, 7.0.9](https://docs.spring.io/spring-framework/docs/7.0.9/javadoc-api/org/springframework/resilience/annotation/Retryable.html)
- [Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html) and [Boot 4.0 requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html)
- [Resilience4j Spring setup](https://resilience4j.readme.io/docs/getting-started-3) and [retry policies](https://resilience4j.readme.io/docs/retry)
- [Failsafe retry policies](https://failsafe.dev/retry/)
