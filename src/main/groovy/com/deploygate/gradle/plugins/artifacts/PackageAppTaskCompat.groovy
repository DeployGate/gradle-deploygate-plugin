package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin

import javax.annotation.Nonnull

//import com.android.build.gradle.api.ApplicationVariant
//import com.android.build.gradle.api.BaseVariantOutput

class PackageAppTaskCompat {
    private PackageAppTaskCompat() {
    }

    @Nonnull
    //    static ApkInfo getApkInfo(com.android.build.gradle.tasks.PackageApplication packageAppTask) {
    static ApkInfo getApkInfo(packageAppTask) {
        String variantName = packageAppTask.name
        Collection<String> apkNames = packageAppTask.outputScope.apkDatas*.outputFileName
        File outputDir = packageAppTask.outputDirectory
        boolean isUniversal = apkNames.size() == 1
        boolean isSigningReady = hasSigningConfig(packageAppTask)

        return new DirectApkInfo(
                variantName,
                new File(outputDir, (String) apkNames[0]),
                isSigningReady,
                isUniversal,
        )
    }

    private static boolean hasSigningConfig(packageAppTask) {
        if (!AndroidGradlePlugin.isSigningConfigCollectionSupported()) {
            return packageAppTask.signingConfig != null
        } else {
            return packageAppTask.signingConfig != null && !packageAppTask.signingConfig.isEmpty()
        }
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