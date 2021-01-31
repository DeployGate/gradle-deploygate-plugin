#!/usr/bin/env bash

# Do not use "e"
set -uxo pipefail

./gradlew testUnrollAcceptanceTest

while read TEST_AGP_VERSION TEST_GRADLE_VERSION NO_AAB_SUPPORT NO_KTS_SUPPORT; do
  export TEST_AGP_VERSION TEST_GRADLE_VERSION NO_AAB_SUPPORT NO_KTS_SUPPORT
  ./gradlew testPluginAcceptanceTest
done < <(cat<<EOF
3.0.0 4.1 true true
3.1.0 4.4 true true
3.2.0 4.6 true false
3.3.2 4.10.1 false false
3.5.1 5.4.1 false false
3.6.0 5.6.4 false false
4.0.0 6.1.1 false false
4.1.0 6.5 false false
4.2.0-beta04 6.7.1 false false
7.0.0-alpha05 6.8 false false
EOF
)
