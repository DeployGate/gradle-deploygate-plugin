package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

interface AabInfo {
    @NotNull
    String getVariantName()

    @Nullable
    File getAabFile()
}