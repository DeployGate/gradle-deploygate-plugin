#!/usr/bin/env bash

set -eu

if [[ "$(cat VERSION)" != "${CIRCLE_TAG:-}" ]]; then
  echo "tag and version verification failed" 1>&2
  echo "Required tag was $(cat VERSION) but ${CIRCLE_TAG:-not found} had come." 1>&2
  exit 1
fi

./gradlew clean build bintrayUpload
