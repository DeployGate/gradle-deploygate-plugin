package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin

//import com.android.build.gradle.api.ApplicationVariant
//import com.android.build.gradle.api.BaseVariantOutput

class ApkInfoCompat {
    private ApkInfoCompat() {
    }

    static ApkInfo blank(String name) {
        return new BlankApkInfo(name)
    }

    static ApkInfo from(/*ApplicationVariant*/ applicationVariant, /*BaseVariantOutput*/ variantOutput) {
        if (AndroidGradlePlugin.isBefore3xx()) {
            return new ApkInfoCompatBefore300Preview(applicationVariant, variantOutput)
        } else if (AndroidGradlePlugin.is3xxPreview()) {
            return new ApkInfoCompat300Preview(applicationVariant, variantOutput)
        } else {
            return new ApkInfoCompatLatest(applicationVariant, variantOutput)
        }
    }

    private static class BlankApkInfo implements ApkInfo {
        private final String name

        private BlankApkInfo(String name) {
            this.name = name
        }

        @Override
        String getVariantName() {
            return name
        }

        @Override
        File getApkFile() {
            return null
        }

        @Override
        boolean isSigningReady() {
            return true
        }

        @Override
        boolean isUniversalApk() {
            return true
        }
    }

    // Keep the latest just extend BaseApkInfo!
    private static class ApkInfoCompatLatest extends BaseApkInfo {
        ApkInfoCompatLatest(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }
    }

    private static abstract class BaseApkInfo implements ApkInfo {
        // Remove comment-out while debugging
//        protected final ApplicationVariant applicationVariant
//        protected final BaseVariantOutput variantOutput
        protected def applicationVariant
        protected def variantOutput

        BaseApkInfo(applicationVariant, variantOutput) {
            this.applicationVariant = applicationVariant
            this.variantOutput = variantOutput
        }

        @Override
        String getVariantName() {
            return applicationVariant.name
        }

        @Override
        File getApkFile() {
            return variantOutput.outputFile
        }

        @Override
        boolean isSigningReady() {
            return true
        }

        @Override
        boolean isUniversalApk() {
            return variantOutput.filters.empty
        }
    }

    private static class ApkInfoCompatBefore300Preview extends BaseApkInfo {
        ApkInfoCompatBefore300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        String getVariantName() {
            return variantOutput.name
        }

        @Override
        boolean isUniversalApk() {
            return variantOutput.outputs.get(0).filters.empty
        }
    }

    private static class ApkInfoCompat300Preview extends ApkInfoCompatBefore300Preview {

        ApkInfoCompat300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        File getApkFile() {
            return applicationVariant.variantData.scope.apkLocation
        }

        @Override
        boolean isSigningReady() {
            if (variantOutput.hasProperty('variantOutputData')) {
                return variantOutput.variantOutputData.variantData.signed
            } else {
                return super.isSigningReady()
            }
        }
    }
}