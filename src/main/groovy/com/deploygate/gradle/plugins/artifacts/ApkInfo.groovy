package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

interface ApkInfo {
    @NotNull
    String getVariantName()

    @Nullable
    File getApkFile()

    boolean isSigningReady()

    boolean isUniversalApk()
}