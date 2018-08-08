package com.deploygate.gradle.plugins.utils

import org.gradle.api.Project

class AndroidPlatformUtils {
    private static AGPVersion AGP_VERSION

    static String getAapt2Location(Project project) {
        return project.deploygate.aapt2Path ?: System.getenv('DEPLOYGATE_APPT2_PATH') ?: new File(project.android.sdkDirectory, "build-tools/${getBuildToolsVersion(project)}/aapt2").toString()
    }

    static AGPVersion getAGPVersion() {
        if (!AGP_VERSION) {
            AGP_VERSION = AndroidPlatformUtils.AGPVersion.parse(getVersionString())
        }

        return AGP_VERSION
    }

    static boolean isAppBundleSupported() {
        return getAGPVersion().major >= 3 && getAGPVersion().minor >= 2
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

    private static class AGPVersion {
        final String fullVersion
        final int major
        final int minor
        final int patch
        final String identifier

        AGPVersion(String fullVersion, int major, int minor, int patch, String identifier) {
            this.fullVersion = fullVersion
            this.major = major
            this.minor = minor
            this.patch = patch
            this.identifier = identifier
        }

        def static parse(String fullVersion) {
            def t = fullVersion.split("-", 1)

            def versions = t[0].split("\\.")
            def identifier = t.size() > 1 ? t[1] : null

            def major = Integer.parseInt(versions[0])
            def minor = Integer.parseInt(versions[1])
            def patch = versions.length > 3 ? Integer.parseInt(versions[2]) : 0

            return new AGPVersion(fullVersion, major, minor, patch, identifier)
        }

        boolean isBefore300Preview() {
            return major < 3
        }

        boolean is300Preview() {
            return fullVersion.startsWith("3.0.0-")
        }

        boolean isBefore320() {
            return major == 3 && minor < 2
        }
    }
}