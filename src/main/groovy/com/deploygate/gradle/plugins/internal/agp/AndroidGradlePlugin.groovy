package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

class AndroidGradlePlugin {
    static void ifPresent(@NotNull Project project, @NotNull Action<?> onFound) {
        try {
            def agpPlugin = project.plugins.findPlugin("com.android.application")

            if (agpPlugin) {
                checkModelLevel(agpPlugin.class.classLoader, project.logger)
                onFound.execute("dummy")
            } else {
                project.plugins.matching { it.class.name == "com.android.build.gradle.AppPlugin" }.whenPluginAdded { Plugin plugin ->
                    project.logger.warn("com.android.application should be applied before DeployGate plugin")
                    checkModelLevel(plugin.class.classLoader, project.logger)
                    onFound.execute("dummy")
                }
            }
        } catch (Throwable th) {
            project.logger.warn("unexpected error has occurred", th)
        }
    }

    /**
     * Parses the AGP version string into a VersionString object.
     * This method no longer caches the version in a static field to support configuration cache.
     *
     * @param versionString The AGP version string to parse (e.g., "8.1.0")
     * @return Parsed VersionString object, or null if parsing fails
     * @since 3.0.0
     */
    static VersionString getVersion(@NotNull String versionString) {
        return VersionString.tryParse(versionString)
    }

    /**
     * Checks if AGP uses internal signing config data structure.
     * This change was introduced in AGP 8.3.0.
     *
     * @param versionString The AGP version string
     * @return true if AGP version is 8.3.0 or higher
     * @since AGP 8.3.0 https://cs.android.com/android-studio/platform/tools/base/+/ff361912406f0eafc42b6ff2a293ee8a17ff77ee:build-system/gradle-core/src/main/java/com/android/build/gradle/tasks/PackageAndroidArtifact.kt;dlc=c2e97e2ca61a5575ccfb48f9528a11c38d651841
     */
    static boolean isInternalSigningConfigData(@NotNull String versionString) {
        def version = getVersion(versionString)
        return version.major >= 8 && version.minor >= 3
    }

    /**
     * Checks if AGP has the OutputsHandler API on PackageApplication task.
     * This API was introduced in AGP 8.1.0.
     *
     * @param versionString The AGP version string
     * @return true if AGP version is 8.1.0 or higher
     * @since AGP 8.1.0 https://android.googlesource.com/platform/tools/base/+/da5cbdf59f91f7480a5d9615a20f766d19c6034a%5E%21/#F32
     */
    static boolean hasOutputsHandlerApiOnPackageApplication(@NotNull String versionString) {
        def version = getVersion(versionString)
        return version.major >= 8 && version.minor >= 1
    }

    @NotNull
    static String androidAssembleTaskName(@NotNull String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    @NotNull
    static String androidBundleTaskName(@NotNull String variantName) {
        return "bundle${variantName.capitalize()}"
    }

    /**
     * Get the AGP version from a classloader because `plugins` block will separate class loaders
     *
     * @param classLoader might be based on AGP's class loader
     * @return
     */
    static String getVersionString(ClassLoader classLoader) {
        try {
            return classLoader.loadClass("com.android.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        return Integer.MAX_VALUE + ".0.0"
    }

    private static void checkModelLevel(@NotNull ClassLoader classLoader, @NotNull Logger logger) {
        try {
            def modelLevel = classLoader.loadClass("com.android.builder.model.AndroidProject").getField("MODEL_LEVEL_LATEST").get(null).toString().toInteger()

            if (modelLevel > 4) {
                logger.warn("Model level has been upgraded to $modelLevel")
            } else {
                logger.debug("Model version looks fine")
            }
        } catch (Throwable th) {
            logger.warn("the agp might be refactored? unexpected error happened while reading model level", th)
        }
    }
}