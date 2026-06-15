#!/usr/bin/env bash

set -eu

# Publish in a single build to:
#   * Maven Central  -> publishReleasePublicationToMavenRepository (com.deploygate:gradle)
#   * Plugin Portal  -> publishPlugins                            (id com.deploygate)
./gradlew clean build \
    publishReleasePublicationToMavenRepository \
    publishPlugins \
    --stacktrace

# The OSSRH Staging API compatibility endpoint only *stages* the Maven Central upload. This
# POST hands the deployment over to the Central Portal and, with publishing_type=automatic,
# releases it to Maven Central once validation passes. It must originate from the same host
# that performed the upload, so it lives here rather than in a separate job.
#
# CENTRAL_TOKEN_USERNAME/PASSWORD are a Central Portal user token (the legacy OSSRH
# credentials return 401). Skipped when unset so non-publishing invocations stay no-op.
if [[ -n "${CENTRAL_TOKEN_USERNAME:-}" && -n "${CENTRAL_TOKEN_PASSWORD:-}" ]]; then
    auth=$(printf '%s:%s' "${CENTRAL_TOKEN_USERNAME}" "${CENTRAL_TOKEN_PASSWORD}" | base64 | tr -d '\n')
    curl -fsS -X POST \
        -H "Authorization: Bearer ${auth}" \
        "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.deploygate?publishing_type=automatic"
fi
