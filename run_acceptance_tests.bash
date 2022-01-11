#!/usr/bin/env bash

# Do not use "e"
set -uxo pipefail

./gradlew testUnrollAcceptanceTest

while read -r TEST_AGP_VERSION TEST_GRADLE_VERSION; do
  export TEST_AGP_VERSION TEST_GRADLE_VERSION
  ./gradlew testPluginAcceptanceTest
done < <(cat<<EOF
3.5.1 5.4.1
3.6.0 5.6.4
4.0.0 6.1.1
4.1.0 6.5
4.2.0 6.7.1
4.2.0 7.0.2
7.0.0 7.0.2
7.1.0-rc01 7.2
7.2.0-alpha07 7.3
EOF
)
