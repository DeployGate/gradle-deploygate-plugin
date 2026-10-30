# CLAUDE.md — gradle-deploygate-plugin

Project-specific knowledge for working in this repo. Version numbers (minimum
AGP/Gradle/JRE, wrapper version) live in `README.md` / `build.gradle` — treat
those as the source of truth rather than duplicating them here.

## Local verification (reproducing CI)

Gradle commands need to write `~/.gradle` and reach the network, so run them
**outside the command sandbox**. Use a JDK that satisfies the wrapper's Gradle
version (the build/dev baseline is JDK 17).

- **Mock server (required for HTTP specs).** `ApiClientSpec` and the upload
  acceptance specs talk to `TEST_SERVER_URL`; without it they hit
  `https://deploygate.com` and fail with `NetworkFailure` / `Connection reset`.
  Start it and export the URL:
  ```
  docker run -d --name dg-mock -p 3000:3000 ghcr.io/deploygate/deploygate-mock-server:main
  # Apple Silicon: add --platform linux/amd64
  export TEST_SERVER_URL=http://localhost:3000
  ```
- **Unit tests:** `./gradlew test` (needs `TEST_SERVER_URL`). These are
  `ProjectBuilder`-based and run on the wrapper's Gradle.
- **Plugin acceptance vs a specific AGP/Gradle:**
  ```
  TEST_AGP_VERSION=8.0.0 TEST_GRADLE_VERSION=8.0 \
    ./gradlew --no-daemon testPluginAcceptanceTest --tests "*ConfigurationCacheSpec*"
  ```
  **`--no-daemon` is mandatory** when setting `TEST_GRADLE_VERSION`: the Gradle
  daemon caches env vars from its first start, so a reused daemon reads a stale
  value and GradleRunner throws `'null' is not a valid Gradle version string`.
- **Unroll acceptance:** `./gradlew testUnrollAcceptanceTest`.
- **Format/lint:** `./gradlew spotlessApply`.

## Build / version gotchas

- **ProjectBuilder + Gradle 8.12/8.13** throws `Problems service is not
  initialized` in the unit tests (gradle/gradle#31862); fixed in 8.14. Keep the
  wrapper off 8.12/8.13.
- **Spotless 8.x** requires Gradle 8.1+ and Java 17. It is applied only on
  Gradle 8.1+ (guard in `build.gradle`) so the lowest acceptance-matrix Gradle
  row still validates the stated minimum.
- The **wrapper Gradle is the build toolchain**, intentionally separate from the
  minimum Gradle that *users* of the plugin need.
- CI's `.github/actions/setup-java` installs Java 11 for `sdkmanager`, then the
  `java-version` input becomes the active JDK; build jobs pass `java-version: 17`.

## Release

- The version is in `src/main/resources/VERSION` (repo-root `VERSION` is a
  symlink). Bump it in a `chore: bump up version to X.Y.Z` commit.
- `.github/workflows/release.yml` asserts `VERSION` equals the pushed tag (no
  `v` prefix) — tag exactly `X.Y.Z`.
- Keep **both** `README.md` and `README_JP.md` in sync (compatibility tables,
  notes). Android Studio code names come from the official AGP compatibility
  table.
- `CHANGELOG.md`: add a `## Unreleased` section only when there are entries;
  rename it to `## ver X.Y.Z` at release time.

## PR / review conventions

- Reply to review comments — **including bots (devin, gemini, Copilot)** — in
  the comment thread with an `@mention` of the author:
  `gh api repos/{owner}/{repo}/pulls/{pr}/comments/{id}/replies -f body=...`
  (not as a top-level PR comment).
- Bot findings are suggestions to verify, not orders. Push back with evidence
  when they are wrong for this codebase.
