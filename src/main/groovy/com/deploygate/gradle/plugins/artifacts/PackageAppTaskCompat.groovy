package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import groovy.transform.PackageScope
import org.gradle.api.Project

import javax.annotation.Nonnull

class PackageAppTaskCompat {
    private PackageAppTaskCompat() {
    }

    @Nonnull
    static ApkInfo getApkInfo(@Nonnull /* PackageApplication */ packageAppTask, @Nonnull String variantName) {
        // outputScope is retrieved by the reflection
        Collection<String> apkNames = getApkNames(packageAppTask)
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

    @Nonnull
    static AabInfo getAabInfo(@Nonnull /* PackageApplication */ packageAppTask, @Nonnull String variantName, @Nonnull Project project) {
        final String aabName

        if (AndroidGradlePlugin.isAppBundleArchiveNameChanged()) {
            // outputScope is retrieved by the reflection
            Collection<String> apkNames = getApkNames(packageAppTask)
            aabName = ((String) apkNames[0]).replaceFirst("\\.apk\$", ".aab")
        } else {
            aabName = "${project.properties["archivesBaseName"] as String}.aab"
        }

        def outputDir = new File(project.buildDir, "outputs/bundle/${variantName}")

        return new DirectAabInfo(
                variantName,
                new File(outputDir, aabName),
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

    static Collection<String> getApkNames(packageAppTask) {
        if (!AndroidGradlePlugin.isOutputFilenameDesignChanged()) {
            return packageAppTask.outputScope.apkDatas*.outputFileName
        } else {
            return packageAppTask.getApkNames()
        }
    }
}