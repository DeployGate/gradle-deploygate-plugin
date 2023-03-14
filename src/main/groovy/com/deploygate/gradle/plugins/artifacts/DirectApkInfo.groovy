package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
class DirectApkInfo implements ApkInfo {
    @NotNull
    private final String variantName
    @Nullable
    private final File apkFile
    private final boolean signingReady
    private final boolean universalApk

    DirectApkInfo(@NotNull String variantName, @Nullable File apkFile, boolean signingReady, boolean universalApk) {
        this.variantName = variantName
        this.apkFile = apkFile
        this.signingReady = signingReady
        this.universalApk = universalApk

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
    File getApkFile() {
        return apkFile
    }

    @Override
    boolean isSigningReady() {
        return signingReady
    }

    @Override
    boolean isUniversalApk() {
        return universalApk
    }
}
