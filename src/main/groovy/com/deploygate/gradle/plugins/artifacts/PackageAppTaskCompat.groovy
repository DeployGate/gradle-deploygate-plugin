package com.deploygate.gradle.plugins.artifacts

import groovy.transform.PackageScope
import org.jetbrains.annotations.NotNull

class PackageAppTaskCompat {
    private PackageAppTaskCompat() {
    }

    @NotNull
    static ApkInfo getApkInfo(@NotNull /* PackageApplication */ packageAppTask, @NotNull String variantName) {
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

    @NotNull
    static AabInfo getAabInfo(@NotNull /* PackageApplication */ packageAppTask, @NotNull String variantName, @NotNull File buildDir) {
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