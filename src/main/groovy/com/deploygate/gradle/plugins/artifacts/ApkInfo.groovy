package com.deploygate.gradle.plugins.artifacts

interface ApkInfo {
    String getVariantName()

    File getApkFile()

    boolean isSigningReady()

    boolean isUniversalApk()
}