package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import javax.annotation.Nonnull

class GradleCompat {
    private static VersionString GRADLE_VERSION

    private GradleCompat() {
    }

    static void init(@Nonnull Project project) {
        GRADLE_VERSION = VersionString.tryParse(project.gradle.gradleVersion)
    }

    @Nonnull
    static VersionString getVersion() {
        if (!GRADLE_VERSION) {
            throw new IllegalStateException("must be initialized")
        }

        return GRADLE_VERSION
    }

    static <T> Provider<T> forUseAtConfigurationTime(Provider<T> provider) {
        if (getVersion().major >= 7) {
            // removed since 7.0 ref: https://github.com/gradle/gradle/issues/15600
            return provider
        } else {
            return provider.forUseAtConfigurationTime()
        }
    }
}
