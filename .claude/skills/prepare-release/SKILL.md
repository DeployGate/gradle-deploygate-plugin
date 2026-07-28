---
name: prepare-release
description: Use when cutting a new release of this plugin — bumping the version, updating the changelog, and refreshing the README compatibility tables before opening the release PR. Triggers on "prepare a release", "cut X.Y.Z", "bump the version", "release prep".
---

# Prepare a release

Stage the release changes on a branch and stop for human approval before
committing/pushing. See `CLAUDE.md` for the version/tag facts.

## Steps

1. **Branch** off `master`: `chore/release-X.Y.Z` (or extend an existing
   release-prep branch).

2. **Bump the version** in `src/main/resources/VERSION` to `X.Y.Z` (repo-root
   `VERSION` is a symlink to it). `release.yml` asserts this equals the git tag,
   so it must match the tag you will push (no `v` prefix).

3. **CHANGELOG.md** — rename the `## Unreleased` section to `## ver X.Y.Z`. List
   user-facing changes and a `### Breaking Changes` subsection if any. Reference
   PRs as `[#NNN](...)`. (Do not list dev-only tooling like Spotless bumps.)

4. **README.md and README_JP.md (keep both in sync):**
   - Version-compatibility table: set the right-hand column for newly
     unsupported AGP rows to `\>=2.4.0,\<X.Y.0`; add Android Studio code names
     for new AGP rows from the official AGP compatibility table
     (https://developer.android.com/build/releases/about-agp).
   - Update the Gradle-compatibility and Binary-compatibility (min JRE) tables
     and the requirement note if the minimums changed.
   - When editing the table, keep columns aligned (a small reformat script that
     pads each column to its max width is the reliable way).

5. **Verify**: run the relevant checks via the `verify-locally` skill.

6. **Stop and confirm with the user** before committing. Then commit in logical
   units (e.g. `chore: bump up version to X.Y.Z` for VERSION+CHANGELOG, separate
   docs commit), open the PR, and watch CI.

## After merge

- Tag `X.Y.Z` exactly (matches `VERSION`); `release.yml` publishes on the tag.
