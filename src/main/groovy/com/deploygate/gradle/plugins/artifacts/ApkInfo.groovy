package com.deploygate.gradle.plugins.artifacts

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

interface ApkInfo {
    @Input
    @NotNull
    String getVariantName()

    @InputFile
    @Optional
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @Nullable
    File getApkFile()

    @Input
    boolean isSigningReady()

    @Input
    boolean isUniversalApk()
}