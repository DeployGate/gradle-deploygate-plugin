package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger

import javax.annotation.Nonnull

class AndroidGradlePlugin {
    private static VersionString AGP_VERSION

    static void ifPresent(@Nonnull Project project, @NotNull Action<?> onFound) {
        try {
            def agpPlugin = project.plugins.findPlugin("com.android.application")

            if (agpPlugin) {
                AGP_VERSION = VersionString.tryParse(getVersionString(agpPlugin.class.classLoader))
                checkModelLevel(agpPlugin.class.classLoader, project.logger)
                onFound.execute("dummy")
            } else {
                project.plugins.matching { it.class.name == "com.android.build.gradle.AppPlugin" }.whenPluginAdded { Plugin plugin ->
                    project.logger.warn("com.android.application should be applied before DeployGate plugin")
                    AGP_VERSION = VersionString.tryParse(getVersionString(plugin.class.classLoader))
                    checkModelLevel(agpPlugin.class.classLoader, project.logger)
                    onFound.execute("dummy")
                }
            }
        } catch (Throwable th) {
            project.logger.warn("unexpected error has occurred", th)
        }
    }

    static VersionString getVersion() {
        if (!AGP_VERSION) {
            AGP_VERSION = VersionString.tryParse(getVersionString())
        }

        return AGP_VERSION
    }

    @Nonnull
    static String androidAssembleTaskName(@Nonnull String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    @Nonnull
    static String androidBundleTaskName(@Nonnull String variantName) {
        return "bundle${variantName.capitalize()}"
    }

    /**
     * Get the AGP version from a classloader because `plugins` block will separate class loaders
     *
     * @param classLoader might be based on AGP's class loader
     * @return
     */
    private static String getVersionString(ClassLoader classLoader) {
        try {
            return classLoader.loadClass("com.android.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        return Integer.MAX_VALUE + ".0.0"
    }

    private static void checkModelLevel(@Nonnull ClassLoader classLoader, @Nonnull Logger logger) {
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