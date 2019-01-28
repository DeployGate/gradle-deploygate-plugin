package com.deploygate.gradle.plugins.artifacts

class DirectApkInfo implements ApkInfo {
    private final String variantName
    private final File apkFile
    private final boolean signingReady
    private final boolean universalApk

    DirectApkInfo(String variantName, File apkFile, boolean signingReady, boolean universalApk) {
        this.variantName = variantName
        this.apkFile = apkFile
        this.signingReady = signingReady
        this.universalApk = universalApk
    }

    @Override
    String getVariantName() {
        return variantName
    }

    @Override
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
