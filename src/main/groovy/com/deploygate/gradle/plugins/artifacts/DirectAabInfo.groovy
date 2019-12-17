package com.deploygate.gradle.plugins.artifacts

import com.google.common.annotations.VisibleForTesting

import javax.annotation.Nonnull
import javax.annotation.Nullable

@VisibleForTesting
class DirectAabInfo implements AabInfo {
    @Nonnull
    private final String variantName
    @Nullable
    private final File aabFile

    DirectAabInfo(@Nonnull String variantName, @Nullable File aabFile) {
        this.variantName = variantName
        this.aabFile = aabFile

        if (!variantName) {
            throw new IllegalArgumentException("variantName must not be null or empty")
        }
    }

    @Override
    @Nonnull
    String getVariantName() {
        return variantName
    }

    @Override
    @Nullable
    File getAabFile() {
        return aabFile
    }
}
