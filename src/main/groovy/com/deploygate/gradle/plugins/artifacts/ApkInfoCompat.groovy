package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.utils.AndroidPlatformUtils

//import com.android.build.gradle.api.ApplicationVariant
//import com.android.build.gradle.api.BaseVariantOutput

class ApkInfoCompat {
    private ApkInfoCompat() {
    }

    static ApkInfo blank(String name) {
        return new BlankApkInfo(name)
    }

    static ApkInfo from(/*ApplicationVariant*/ applicationVariant, /*BaseVariantOutput*/ variantOutput) {
        def agpVersion = AndroidPlatformUtils.getAGPVersion()

        if (agpVersion.isBefore300Preview()) {
            return new ApkInfoCompatBefore300Preview(applicationVariant, variantOutput)
        } else if (agpVersion.is300Preview()) {
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

        @Override
        SigningConfig getSigningConfig() {
            return null
        }
    }

    private static abstract class BaseApkInfo implements ApkInfo {
        // Remove comment-out while debugging
//        protected final ApplicationVariant applicationVariant
//        protected final BaseVariantOutput variantOutput
        protected def applicationVariant
        protected def variantOutput
        private final SigningConfig signingConfig

        BaseApkInfo(applicationVariant, variantOutput) {
            this.applicationVariant = applicationVariant
            this.variantOutput = variantOutput

            def signingConfig = this.applicationVariant.packageApplication.signingConfig

            if (signingConfig != null) {
                this.signingConfig = new SigningConfig(signingConfig.storeFile, signingConfig.keyPassword, signingConfig.keyAlias, signingConfig.storePassword)
            } else {
                this.signingConfig = null
            }
        }

        @Override
        String getVariantName() {
            return variantOutput.name
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
            return variantOutput.outputs.get(0).filters.empty
        }

        @Override
        SigningConfig getSigningConfig() {
            return signingConfig
        }
    }

    private static class ApkInfoCompatBefore300Preview extends BaseApkInfo {
        ApkInfoCompatBefore300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }
    }

    private static class ApkInfoCompat300Preview extends BaseApkInfo {

        ApkInfoCompat300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        File getApkFile() {
            return applicationVariant.variantData.scope.apkLocation
        }

        @Override
        boolean isSigningReady() {
            if (output.hasProperty('variantOutputData')) {
                return output.variantOutputData.variantData.signed
            } else {
                return super.isSigningReady()
            }
        }
    }

    private static class ApkInfoCompatLatest extends BaseApkInfo {
        ApkInfoCompatLatest(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        boolean isUniversalApk() {
            return variantOutput.filters.empty
        }
    }
}