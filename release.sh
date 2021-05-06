#!/usr/bin/env bash

set -eu

./gradlew clean build publishReleasePublicationToMavenRepository --stacktrace
