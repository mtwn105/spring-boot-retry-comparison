# Spring Boot Retry Comparison

![Java 21](https://img.shields.io/badge/Java-21-orange)
![Maven 3.9+](https://img.shields.io/badge/Maven-3.9%2B-blue)

**Explore four ways to retry a failing operation in Spring Boot—with runnable examples and a shared test suite.**

Compare Spring Retry, Spring Framework 7's built-in retry support, Resilience4j, and Failsafe using the same inventory lookup. Each implementation demonstrates successful retries, exhausted attempts, permanent errors, and backoff behavior.

The examples run locally as console applications. You only need Java and Maven—no database, Docker, API keys, or external services.

[Quick start](#quick-start) · [Implementations](#implementations) · [How it works](#how-it-works) · [Tests](#tests) · [Contributing](#contributing)

## Quick start

### Prerequisites

- JDK **21**
- Maven **3.9+**
- Access to Maven Central for the initial dependency download
- Bash to use the optional run-all script

### Clone, build, and run

```bash
git clone https://github.com/mtwn105/spring-boot-retry-comparison.git
cd spring-boot-retry-comparison

mvn -B verify
bash scripts/run-all.sh
```

Each application prints the scenario results and exits. A failed scenario contract causes a nonzero exit.

Example output from the Spring Retry implementation:

```text
Implementation: Spring Retry
immediate-success attempts=1 outcome=IN_STOCK offsetsMs=[0.0]
fail-twice-then-success attempts=3 outcome=IN_STOCK offsetsMs=[0.0, 101.3, 301.8]
exhausted attempts=3 outcome=TransientInventoryException offsetsMs=[0.0, 100.5, 301.2]
permanent-error attempts=1 outcome=PermanentInventoryException offsetsMs=[0.0]
All four scenario contracts passed.
```

Timing varies between runs. Offsets are measured from the first invocation; they are not individual retry delays or performance rankings.

### Run one implementation

From the repository root:

```bash
mvn -B -pl spring-framework-retry-demo -am verify
java -jar spring-framework-retry-demo/target/spring-framework-retry-demo.jar
```

Replace `spring-framework-retry-demo` with any implementation module below. The `-am` flag also builds the shared module. After building the whole project, you can run any application's jar independently.

## Implementations

| Module | Spring Boot | Approach | Three total calls |
| --- | --- | --- | --- |
| [Spring Retry](spring-retry-demo/) | 3.5.16 | `@EnableRetry` + legacy `@Retryable` | `maxAttempts = 3` |
| [Spring Framework retry](spring-framework-retry-demo/) | 4.0.8 | `@EnableResilientMethods` + Framework `@Retryable` | `maxRetries = 2` |
| [Resilience4j](resilience4j-demo/) | 3.5.16 | `@Retry` + named YAML policy | `max-attempts: 3` |
| [Failsafe](failsafe-demo/) | 3.5.16 | Explicit `RetryPolicy` around the operation | `.withMaxAttempts(3)` |

**Watch the attempt count:** Framework retry counts retries *after* the initial call. The other settings shown above include the initial call.

The modules isolate Boot 3 and Boot 4 dependency graphs. Library versions are pinned in the [root POM](pom.xml): Spring Retry **2.0.13**, Resilience4j **2.3.0**, and Failsafe **3.3.2**. Framework retry comes through Boot 4's dependency management.

Spring Retry is [archived](https://github.com/spring-attic/spring-retry); this example remains useful for understanding existing applications and migration. The Resilience4j example uses its Boot 3 starter. These examples do not establish Boot 4 compatibility for that starter.

## How it works

All implementations use a shared, deterministic inventory probe and the same policy:

- Allow **three total invocations** per operation.
- Retry `TransientInventoryException`; propagate permanent errors immediately.
- Back off **100ms**, then **200ms**, with a configured maximum of **1000ms**.
- Propagate the last original exception after exhaustion.

| Scenario | Calls | Result |
| --- | --- | --- |
| Immediate success | 1 | `IN_STOCK` |
| Fail twice, then succeed | 3 | `IN_STOCK` |
| Persistent transient failure | 3 | `TransientInventoryException` |
| Permanent error | 1 | `PermanentInventoryException` |

The annotation-based implementations also demonstrate **self-invocation bypassing retry advice**. Failsafe's explicit wrapper remains active when invoked from another method in the same object. The shared tests exercise both behaviors.

This is a synchronous, in-process learning project. Jitter and fallbacks are deliberately omitted to keep behavior comparable. With only two retry delays, the configured backoff cap is never reached. HTTP timeouts, idempotency, reactive execution, circuit breaker composition, and production load testing are outside the examples' scope.

## Project structure

```text
spring-boot-retry-comparison/
├── pom.xml
├── retry-demo-common/             # Inventory probe, scenarios, shared test contract
├── spring-retry-demo/             # Legacy Spring Retry annotations
├── spring-framework-retry-demo/   # Built-in Framework 7 retry
├── resilience4j-demo/             # Annotation + YAML configuration
├── failsafe-demo/                 # Programmatic retry policy
├── scripts/run-all.sh
├── validation/                    # Recorded results
└── VALIDATION.md
```

Start with a module's `InventoryService.java` and `DemoApplication.java`. For Resilience4j, also check `src/main/resources/application.yml`. Each module's README links directly to its implementation and test.

## Tests

```bash
mvn -B verify
```

**28 tests passed** in the recorded run: seven per implementation, with no failures, errors, or skipped tests.

Six shared checks cover immediate success, eventual success, exhaustion with original exception identity, permanent-error exclusion, independent operation state, and exponential backoff lower bounds. Each implementation adds a self-invocation check. Tests load the real Spring Boot context, including YAML configuration.

See the [validation record](VALIDATION.md), [test summary](validation/test-summary.json), and [console output](validation/run-all.txt) for evidence and coverage limits. Sources target Java 17 bytecode; execution was checked on JDK 21.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Dependency transfer or DNS error | Check Maven Central access, then retry with `mvn -U verify`. |
| Invalid release or unsupported class version | Confirm `java -version` and `mvn -version` use the intended JDK. |
| Shared artifact not found | Build from the root with `-pl <module> -am verify`, or run root `mvn install` before building directly inside a module. |
| Only one call instead of retries | Check the annotation import, activation, Spring bean call path, and named YAML policy. |
| Executable jar missing | Run `mvn verify`; `mvn test` does not package applications. |

Use `mvn clean` to remove generated build files. Applications exit automatically and leave no services running.

## Contributing

Bug reports, clearer explanations, and focused examples are welcome.

1. [Open an issue](https://github.com/mtwn105/spring-boot-retry-comparison/issues) to describe a bug or discuss a larger change.
2. Fork the repository and create a branch for your change.
3. Keep equivalent retry behavior across implementations, or explain why a scenario differs.
4. Run `mvn -B verify` and `bash scripts/run-all.sh`.
5. Open a pull request describing the change and how you verified it.

For bug reports, include Java/Maven versions, the affected module, reproduction commands, and relevant output. Remove credentials and private configuration before sharing logs.

## Further reading

- [Spring Retry source and maintenance status](https://github.com/spring-attic/spring-retry)
- [Spring Framework resilience features](https://docs.spring.io/spring-framework/reference/core/resilience.html)
- [Resilience4j Spring integration](https://resilience4j.readme.io/docs/getting-started-3) and [retry configuration](https://resilience4j.readme.io/docs/retry)
- [Failsafe retry policies](https://failsafe.dev/retry/)

Created by [mtwn105](https://github.com/mtwn105). More technical writing on [Medium](https://medium.com/@mtwn105).
