#!/usr/bin/env bash

set -uo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
failed=0

run_check() {
  local name="$1"
  local directory="$2"
  shift 2

  printf '\n=== %s ===\n' "$name"
  if ! (cd "$ROOT_DIR/$directory" && "$@"); then
    printf 'FAILED: %s\n' "$name" >&2
    failed=1
  fi
}

run_check "Backend health-check regression" backend mvn -Dtest=HealthCheckTest test

printf '\n=== E2E tests ===\n'
e2e_ready=1
for url in http://localhost:5173 http://localhost:8080/actuator/health; do
  if ! curl --fail --silent --show-error --max-time 3 "$url" >/dev/null; then
    printf 'E2E prerequisite is unavailable: %s\n' "$url" >&2
    e2e_ready=0
  fi
done

if [[ "$e2e_ready" -eq 1 ]]; then
  run_check "UC001 and UC002 E2E regression" e2e pnpm test:regression
else
  printf 'SKIPPED: E2E scenarios (start the frontend and backend, then rerun)\n' >&2
  failed=1
fi

if [[ "$failed" -eq 0 ]]; then
  printf '\nRegression tests passed.\n'
else
  printf '\nRegression tests failed or could not complete; see messages above.\n' >&2
fi

exit "$failed"
