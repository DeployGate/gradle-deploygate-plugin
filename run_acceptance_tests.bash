#!/usr/bin/env bash

# Do not use "e"
set -uxo pipefail

./gradlew testUnrollAcceptanceTest

while read -r TEST_AGP_VERSION TEST_GRADLE_VERSION; do
  export TEST_AGP_VERSION TEST_GRADLE_VERSION
  ./gradlew testPluginAcceptanceTest
done < <(cat<<EOF
4.2.0 6.7.1
4.2.0 7.0.2
7.0.0 7.0.2
7.1.0 7.2
7.2.0 7.3.3
7.3.0 7.4.2
7.4.0 7.5
8.0.0 8.0
8.1.0 8.0
8.2.0 8.2
8.3.0 8.4
8.4.0 8.6
8.5.0 8.7
8.6.0 8.7
8.7.0 8.9
EOF
)
