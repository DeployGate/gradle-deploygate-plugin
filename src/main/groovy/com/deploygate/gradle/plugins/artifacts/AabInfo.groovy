package com.deploygate.gradle.plugins.artifacts

import javax.annotation.Nonnull
import javax.annotation.Nullable

interface AabInfo {
    @Nonnull
    String getVariantName()

    @Nullable
    File getAabFile()
}