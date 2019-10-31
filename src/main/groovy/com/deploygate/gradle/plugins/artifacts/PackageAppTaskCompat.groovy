package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import groovy.transform.PackageScope

import javax.annotation.Nonnull

class PackageAppTaskCompat {
    private PackageAppTaskCompat() {
    }

    @Nonnull
    static ApkInfo getApkInfo(@Nonnull /* com.android.build.gradle.tasks.PackageApplication */ packageAppTask) {
        String variantName = packageAppTask.name
        // outputScope is retrieved by the reflection
        Collection<String> apkNames = packageAppTask.outputScope.apkDatas*.outputFileName
        File outputDir = getOutputDirectory(packageAppTask)
        boolean isUniversal = apkNames.size() == 1
        boolean isSigningReady = hasSigningConfig(packageAppTask)

        return new DirectApkInfo(
                variantName,
                new File(outputDir, (String) apkNames[0]),
                isSigningReady,
                isUniversal,
        )
    }

    @PackageScope
    static boolean hasSigningConfig(packageAppTask) {
        if (!AndroidGradlePlugin.isSigningConfigCollectionSupported()) {
            return packageAppTask.signingConfig != null
        } else if (!AndroidGradlePlugin.isSigningConfigProviderSupported()) {
            return packageAppTask.signingConfig != null && !packageAppTask.signingConfig.isEmpty()
        } else {
            return packageAppTask.signingConfig != null && !packageAppTask.signingConfig.signingConfigFileCollection // no need to check `empty` for now
        }
    }

    static File getOutputDirectory(packageAppTask) {
        if (!AndroidGradlePlugin.isOutputDirectoryProviderSupported()) {
            return packageAppTask.outputDirectory
        } else {
            return packageAppTask.outputDirectory.getAsFile().get()
        }
    }
}