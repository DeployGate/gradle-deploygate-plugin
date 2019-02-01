package com.deploygate.gradle.plugins.artifacts

import groovy.transform.PackageScope

import javax.annotation.Nonnull
import javax.annotation.Nullable

@PackageScope
class DirectApkInfo implements ApkInfo {
    @Nonnull
    private final String variantName
    @Nullable
    private final File apkFile
    private final boolean signingReady
    private final boolean universalApk

    DirectApkInfo(@Nonnull String variantName, @Nullable File apkFile, boolean signingReady, boolean universalApk) {
        this.variantName = variantName
        this.apkFile = apkFile
        this.signingReady = signingReady
        this.universalApk = universalApk

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
