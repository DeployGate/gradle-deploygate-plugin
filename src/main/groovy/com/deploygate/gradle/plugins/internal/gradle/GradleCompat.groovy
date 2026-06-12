package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.annotations.NotNull

class GradleCompat {
    private GradleCompat() {
    }

    /**
     * @deprecated No longer needed for initialization
     */
    @Deprecated
    static void init(@NotNull Project project) {
        // No-op for backwards compatibility
    }

    /**
     * Get the current Gradle version from the project
     * @param project the project to get the version from
     * @return the parsed version string
     */
    @NotNull
    static VersionString getVersion(@NotNull Project project) {
        return VersionString.tryParse(project.gradle.gradleVersion)
    }

    /**
     * Kept for source compatibility with existing call sites.
     *
     * {@code Provider#forUseAtConfigurationTime()} was only required on Gradle 6.x, was deprecated
     * in Gradle 7.4, and removed in Gradle 9.0. The minimum supported Gradle is now 8.0, where
     * reading providers at configuration time is the default, so this is a pass-through.
     *
     * ref: https://github.com/gradle/gradle/issues/15600
     */
    static <T> Provider<T> forUseAtConfigurationTime(Provider<T> provider) {
        return provider
    }
}
