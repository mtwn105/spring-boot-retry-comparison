#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
command -v java >/dev/null || { echo "JDK required: java was not found on PATH." >&2; exit 1; }

modules=(spring-retry-demo spring-framework-retry-demo resilience4j-demo failsafe-demo)
for module in "${modules[@]}"; do
    jar="$project_root/$module/target/$module.jar"
    if [[ ! -f "$jar" ]]; then
        echo "Missing $jar. Run mvn verify from the project root first." >&2
        exit 1
    fi
done

for module in "${modules[@]}"; do
    java -jar "$project_root/$module/target/$module.jar"
done
