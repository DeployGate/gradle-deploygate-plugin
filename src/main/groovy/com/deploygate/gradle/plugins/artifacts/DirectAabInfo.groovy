package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
class DirectAabInfo implements AabInfo {
    @NotNull
    private final String variantName
    @Nullable
    private final File aabFile

    DirectAabInfo(@NotNull String variantName, @Nullable File aabFile) {
        this.variantName = variantName
        this.aabFile = aabFile

        if (!variantName) {
            throw new IllegalArgumentException("variantName must not be null or empty")
        }
    }

    @Override
    @NotNull
    String getVariantName() {
        return variantName
    }

    @Override
    @Nullable
    File getAabFile() {
        return aabFile
    }
}
