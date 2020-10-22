package com.deploygate.gradle.plugins.internal

import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.Nullable
import java.util.regex.Pattern

class VersionString implements Comparable<VersionString> {
    private static final Logger LOGGER = LoggerFactory.getLogger(this.getClass())
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?\$")
    private static final Pattern PRERELEASE_TAG_PATTERN = Pattern.compile("^([a-zA-Z]+).*\$")
    private static final Map<String, Integer> PRERELEASE_DELTAS = [
            "alpha": 1,
            "beta": 2,
            "rc": 3,
            "": 100
    ]

    @Nullable
    static VersionString tryParse(@Nullable String version) {
        if (!version) {
            return null
        }

        LOGGER.info(version)

        def versions = version.split("-", 2)

        try {
            def versionMatcher = VERSION_PATTERN.matcher(versions[0])

            if (!versionMatcher.find() || versionMatcher.groupCount() < 2) {
                return null
            }

            def major = versionMatcher.group(1).toInteger()
            def minor = versionMatcher.group(2).toInteger()
            def patch = 0

            if (versionMatcher.groupCount() >= 3) {
                patch = versionMatcher.group(3)?.toInteger() ?: 0
            }

            String prerelease = null
            int metaBuild = 0

            if (versions.length == 2) {
                def prereleaseMatcher = PRERELEASE_TAG_PATTERN.matcher(versions[1])
                if (!prereleaseMatcher.find() || prereleaseMatcher.groupCount() < 1) {
                    return null
                }

                prerelease = prereleaseMatcher.group(1)
                def meta = versions[1].substring(prerelease.length(), versions[1].length())
                metaBuild = meta.length() > 0 ? Math.abs(meta.toInteger()) : 0
            }

            new VersionString(major, minor, patch, prerelease, metaBuild)
        } catch (NumberFormatException ignore) {
            return null
        }
    }

    final int major
    final int minor
    final int patch

    @Nullable
    final String prerelease

    final int metaBuild

    VersionString(int major, int minor, int patch, @Nullable String prerelease, int metaBuild) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.prerelease = prerelease
        this.metaBuild = metaBuild
    }

    @Override
    int compareTo(@NotNull VersionString versionString) {
        return [
                major <=> versionString.major,
                minor <=> versionString.minor,
                patch <=> versionString.patch,
                PRERELEASE_DELTAS[prerelease ?: ""] <=> PRERELEASE_DELTAS[versionString.prerelease ?: ""],
                metaBuild <=> versionString.metaBuild
        ].find {
            it != 0
        } ?: 0
    }

    @Override
    String toString() {
        def builder = new StringBuffer()

        builder.append(major)
        builder.append(".")
        builder.append(minor)
        builder.append(".")
        builder.append(patch)

        if (prerelease != null) {
            builder.append("-")
            builder.append(prerelease)
            builder.append(metaBuild)
        }

        return builder.toString()
    }
}
