# Validation record

Checked on 2026-10-20 UTC in Linux / WSL2 amd64.

## Toolchain

- OpenJDK 21.0.12.1, Ubuntu build `21.0.12.1+1-1-26.04.4-Ubuntu`.
- Apache Maven 3.9.12, using `/usr/lib/jvm/java-21-openjdk-amd64`.
- Sources compile with Java release target 17.

Both tools are available system-wide as `/usr/bin/java` and `/usr/bin/mvn`.

## Completed checks

| Check | Procedure | Result |
| --- | --- | --- |
| Shared production-code compilation | From this directory: `javac --release 17 -d /tmp/retry-common-check retry-demo-common/src/main/java/dev/mtwn105/retry/common/*.java` | Passed on JDK 21. |
| Shared scenario smoke check | Temporary Java driver using those compiled classes: immediate success, two direct transient failures followed by success, original exception identity, immutable timestamp snapshots, permanent failure and negative-input rejection. Passed an unwrapped probe to `DemoSuite` as a negative control. | Passed; the harness correctly rejected the missing retry behavior. This did not load any retry library. |
| POM structure | Parsed all six POM files with Python's XML parser and checked each declared module has a POM. | Passed. This is XML/structure validation, not Maven dependency or lifecycle validation. |
| Shell syntax | `bash -n scripts/run-all.sh` | Passed. |
| Run guard and documentation | Executed the run-all script before packaging; checked local Markdown links. | Script correctly refused to run missing jars; all local project documentation links resolved. |

## Full build attempt

From this directory:

```bash
mvn -B -ntp -Dmaven.repo.local=/tmp/blog-maven-repository verify
```

**Failed during model/dependency resolution:** `Unknown host repo.maven.apache.org`. JUnit and Spring Boot BOM downloads failed. Managed dependency version errors followed because those BOMs were unavailable. This failure does not establish that a requested artifact or version is absent from Maven Central.

That initial agent-side attempt did not compile adapters. The author subsequently built the project from the local terminal. Inspection of the resulting Surefire XML reports confirmed all four suites passed; all four executable jars are present. The author then ran `bash scripts/run-all.sh` and supplied the complete successful console output.

## Successful follow-up evidence

| Module | Tests | Failures | Errors | Skipped | Console scenarios |
| --- | --- | --- | --- | --- | --- |
| spring-retry-demo | 7 | 0 | 0 | 0 | 4 passed |
| spring-framework-retry-demo | 7 | 0 | 0 | 0 | 4 passed |
| resilience4j-demo | 7 | 0 | 0 | 0 | 4 passed |
| failsafe-demo | 7 | 0 | 0 | 0 | 4 passed |

The six inherited contract tests verify immediate success, eventual success, exhaustion with original exception identity, permanent-error exclusion, independent operation state and exponential backoff lower bounds. Each module adds its self-invocation test. The reports were inspected directly from `*/target/surefire-reports/TEST-*.xml`; the earlier DNS failure is historical and no longer a validation blocker for these artifacts.

- [Sanitized test summary](validation/test-summary.json): suite counts and test case names, extracted from local reports without system properties.
- [Console output](validation/run-all.txt): the author-provided successful run, preserved without changing the observed values.

All implementations stopped at three total calls for persistent transient failure, recovered from two transient failures on the third call, and stopped after one permanent error. Measured offsets were consistent with 100ms and 200ms backoff. These few sequential observations are not performance benchmarks.

## Reproduce

From this directory:

```bash
mvn -B verify
bash scripts/run-all.sh
```

Inspect `target/surefire-reports/` in each application module. Each console example should print `All four scenario contracts passed.` and exit successfully.

HTTP behavior, cancellation, concurrency, transactions, circuit breaker ordering, reactive support, recovery callbacks, jitter distribution and performance benchmarking are outside this example's executed coverage. Java 17 execution and clean builds on additional platforms have not been checked.
