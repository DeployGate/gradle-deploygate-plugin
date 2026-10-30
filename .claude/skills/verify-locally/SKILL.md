---
name: verify-locally
description: Use when reproducing this plugin's CI checks locally before pushing — running its unit tests, Spotless, or the AGP/Gradle acceptance specs against the DeployGate mock server. Triggers on "verify locally", "run the tests like CI", "check before pushing", NetworkFailure/Connection reset in ApiClientSpec, or "'null' is not a valid Gradle version".
---

# Verify locally (CI-equivalent)

Reproduce the `build-and-test.yml` jobs on your machine. See `CLAUDE.md` for the
underlying facts and gotchas; this is the runnable procedure.

## Steps

1. **Run outside the command sandbox** — Gradle writes `~/.gradle` and hits the
   network. Use a JDK 17 (the build baseline). `export JAVA_HOME=<jdk17>`.

2. **Start the mock server** (required for `ApiClientSpec` and the upload specs;
   without it they hit `https://deploygate.com` and fail with `NetworkFailure` /
   `Connection reset`):
   ```bash
   docker run -d --name dg-mock -p 3000:3000 ghcr.io/deploygate/deploygate-mock-server:main
   # Apple Silicon: add --platform linux/amd64. If the port is taken, use another and adjust the URL.
   export TEST_SERVER_URL=http://localhost:3000
   until curl -fsI "$TEST_SERVER_URL" >/dev/null 2>&1; do sleep 2; done
   ```

3. **Pick the job to mirror:**
   | CI job | Command |
   |---|---|
   | `unit-test` | `./gradlew publishToMavenLocal test` |
   | `lint` | `./gradlew spotlessApply` |
   | `acceptance-test` | `./gradlew testUnrollAcceptanceTest` |
   | `acceptance-test-runtime-env` (one row) | `TEST_AGP_VERSION=<agp> TEST_GRADLE_VERSION=<gradle> ./gradlew --no-daemon testPluginAcceptanceTest` |

   Add `--tests "*SomeSpec*"` to narrow a run.

4. **Clean up:** `docker rm -f dg-mock`.

## Gotchas

- **`--no-daemon` is mandatory when setting `TEST_GRADLE_VERSION`** — a reused
  daemon caches the env from its first start, so GradleRunner reads a stale value
  and throws `'null' is not a valid Gradle version string`.
- ProjectBuilder unit specs fail with `Problems service is not initialized` on
  Gradle 8.12/8.13 (gradle/gradle#31862) — the wrapper must avoid those.
- To exercise a different build-Gradle without touching the committed wrapper,
  download that distribution and run `gradle -p <projectDir> <task>` from it.
