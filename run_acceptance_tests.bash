#!/usr/bin/env bash

# Do not use "e"
set -uxo pipefail

./gradlew testUnrollAcceptanceTest

# Keep this list in sync with the acceptance-test-runtime-env matrix in
# .github/workflows/build-and-test.yml
while read -r TEST_AGP_VERSION TEST_GRADLE_VERSION; do
  export TEST_AGP_VERSION TEST_GRADLE_VERSION
  ./gradlew testPluginAcceptanceTest
done < <(cat<<EOF
8.0.0 8.0
8.13.0 8.13
9.0.0 9.1.0
9.2.0 9.4.1
EOF
)
