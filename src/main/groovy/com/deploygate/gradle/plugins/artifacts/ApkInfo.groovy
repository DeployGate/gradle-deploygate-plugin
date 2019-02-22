package com.deploygate.gradle.plugins.artifacts

import javax.annotation.Nonnull
import javax.annotation.Nullable

interface ApkInfo {
    @Nonnull
    String getVariantName()

    @Nullable
    File getApkFile()

    boolean isSigningReady()

    boolean isUniversalApk()
}