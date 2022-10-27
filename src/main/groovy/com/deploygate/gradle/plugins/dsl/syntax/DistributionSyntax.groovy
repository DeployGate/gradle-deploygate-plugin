package com.deploygate.gradle.plugins.dsl.syntax

import javax.annotation.Nullable

interface DistributionSyntax {
    /**
     * Set the key of the distribution.
     *
     * @param key
     * @example https://deploygate.com/distributions/<key>
     */
    void setKey(@Nullable String key)

    /**
     * Set the release note of the distribution.
     *
     * @param releaseNote
     */
    void setReleaseNote(@Nullable String releaseNote)
}