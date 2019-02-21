package com.deploygate.gradle.plugins.dsl.syntax


import javax.annotation.Nullable

interface DistributionSyntax {
    void setKey(@Nullable String key)

    void setReleaseNote(@Nullable String releaseNote)
}