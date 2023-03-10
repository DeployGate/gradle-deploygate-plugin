package com.deploygate.gradle.plugins.artifacts

import groovy.transform.PackageScope

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
    static AabInfo getAabInfo(@Nonnull /* PackageApplication */ packageAppTask, @Nonnull String variantName, @Nonnull File buildDir) {
        final String aabName

        // TODO Use Artifact API
        // outputScope is retrieved by the reflection
        Collection<String> apkNames = getApkNames(packageAppTask)
        aabName = ((String) apkNames[0]).replaceFirst("\\.apk\$", ".aab")

        def outputDir = new File(buildDir, "outputs/bundle/${variantName}")

        return new DirectAabInfo(
                variantName,
                new File(outputDir, aabName),
        )
    }

    @PackageScope
    static boolean hasSigningConfig(packageAppTask) {
        return packageAppTask.signingConfigData.resolve() != null
    }

    static File getOutputDirectory(packageAppTask) {
        return packageAppTask.outputDirectory.getAsFile().get()
    }

    static Collection<String> getApkNames(packageAppTask) {
        return packageAppTask.variantOutputs.get().collect { it.outputFileName.get() }
    }
}