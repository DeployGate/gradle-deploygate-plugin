package com.deploygate.gradle.plugins

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.Nullable
import java.util.regex.Pattern

class VersionString {
    private static final Logger LOGGER = LoggerFactory.getLogger(this.getClass())
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?\$")

    @Nullable
    static VersionString tryParse(@Nullable String version) {
        if (!version) {
            return null
        }

        LOGGER.info(version)

        def versions = version.split("-", 2)

        try {
            def matcher = VERSION_PATTERN.matcher(versions[0])

            if (!matcher.find() || matcher.groupCount() < 2) {
                return null
            }

            def major = matcher.group(1).toInteger()
            def minor = matcher.group(2).toInteger()
            def patch = 0

            if (matcher.groupCount() >= 3) {
                patch = matcher.group(3)?.toInteger() ?: 0
            }

            String addition = null

            if (versions.length == 2) {
                addition = versions[1]
            }

            new VersionString(major, minor, patch, addition)
        } catch (NumberFormatException ignore) {
            return null
        }
    }

    final int major
    final int minor
    final int patch

    @Nullable
    final String addition

    VersionString(int major, int minor, int patch, @Nullable String addition) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.addition = addition
    }

    @Override
    String toString() {
        def builder = new StringBuffer()

        builder.append(major)
        builder.append(".")
        builder.append(minor)
        builder.append(".")
        builder.append(patch)

        if (addition != null) {
            builder.append("-")
            builder.append(addition)
        }

        return builder.toString()
    }
}
