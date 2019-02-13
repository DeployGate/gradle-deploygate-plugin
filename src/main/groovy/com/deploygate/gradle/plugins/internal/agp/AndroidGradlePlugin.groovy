package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project

class AndroidGradlePlugin {
    private static VersionString AGP_VERSION

    private static VersionString getVersion() {
        if (!AGP_VERSION) {
            AGP_VERSION = VersionString.tryParse(getVersionString())
        }

        return AGP_VERSION
    }

    static boolean isApplied(Project project) {
        return ['com.android.application', 'android'].any { project.plugins.hasPlugin(it) }
    }

    // FIXME buildtools' aapt2 is not for bundletool
    static String getAapt2Location(Project project) {
        return System.getenv('DEPLOYGATE_APPT2_PATH') ?: new File(project.android.sdkDirectory as File, "build-tools/${getBuildToolsVersion(project)}/aapt2").toString()
    }

    static boolean isAppBundleSupported() {
        return getVersion().major >= 3 && getVersion().minor > 1
    }

    static boolean isSigningConfigCollectionSupported() {
        return getVersion().major >= 3 && getVersion().minor > 2
    }

    static boolean isTaskProviderBased() {
        return getVersion().major >= 3 && getVersion().minor > 2
    }

    private static String getVersionString() {
        try {
            return Class.forName("com.android.builder.model.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        // before 3.1
        try {
            return Class.forName("com.android.builder.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        return Integer.MAX_VALUE + ".0.0"
    }

    private static String getBuildToolsVersion(Project project) {
        try {
            def buildToolRevision = Class.forName('com.android.repository.Revision').getMethod('safeParseRevision', String.class).invoke(null, project.android.buildToolsVersion)

            def klass = Class.forName('com.android.builder.core.AndroidBuilder')

            if (buildToolRevision < klass.getField('MIN_BUILD_TOOLS_REV').get(null)) {
                buildToolRevision = klass.getField('DEFAULT_BUILD_TOOLS_REVISION').get(null)
            }

            return buildToolRevision
        } catch (Throwable ignored) {
            // print error
            return null
        }
    }
}