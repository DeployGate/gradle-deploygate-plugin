package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger

import javax.annotation.Nonnull

class AndroidGradlePlugin {
    private static VersionString AGP_VERSION

    static void init(@Nonnull Project project) {
        try {
            def agpPlugin = project.plugins.findPlugin("com.android.application")

            if (agpPlugin) {
                AGP_VERSION = VersionString.tryParse(getVersionString(agpPlugin.class.classLoader))
                checkModelLevel(agpPlugin.class.classLoader, project.logger)
            } else {
                project.plugins.whenPluginAdded { Plugin plugin ->
                    if (plugin.class.name == "com.android.build.gradle.AppPlugin") {
                        project.logger.warn("com.android.application should be applied before DeployGate plugin")

                        AGP_VERSION = VersionString.tryParse(getVersionString(plugin.class.classLoader))
                        checkModelLevel(agpPlugin.class.classLoader, project.logger)
                    }
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

    static boolean isApplied(Project project) {
        return project.plugins.hasPlugin("com.android.application")
    }

    static boolean isAppBundleSupported() {
        return getVersion().major >= 4 || getVersion().major == 3 && getVersion().minor > 1
    }

    static boolean isSigningConfigCollectionSupported() {
        return getVersion().major >= 4 || getVersion().major == 3 && getVersion().minor > 2
    }

    static boolean isTaskProviderBased() {
        return getVersion().major >= 4 || getVersion().major == 3 && getVersion().minor > 2
    }

    @Nonnull
    static String androidAssembleTaskName(@Nonnull String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    /**
     * Get the AGP version from a classloader because `plugins` block will separate class loaders
     *
     * @param classLoader might be based on AGP's class loader
     * @return
     */
    private static String getVersionString(ClassLoader classLoader) {
        try {
            return classLoader.loadClass("com.android.builder.model.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        // before 3.1
        try {
            return classLoader.loadClass("com.android.builder.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
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