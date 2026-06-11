package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.util.GradleVersion
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
     * Handle forUseAtConfigurationTime compatibility.
     *
     * {@code Provider#forUseAtConfigurationTime()} is only required to read providers at
     * configuration time on Gradle 6.x. It was deprecated in Gradle 7.4 (so calling it on 7.0+
     * emits a deprecation warning) and removed in Gradle 9.0. We therefore only invoke it on
     * Gradle below 7.0 and return the provider untouched otherwise.
     *
     * ref: https://github.com/gradle/gradle/issues/15600
     */
    static <T> Provider<T> forUseAtConfigurationTime(Provider<T> provider) {
        if (GradleVersion.current().baseVersion >= GradleVersion.version("7.0")) {
            return provider
        } else {
            return provider.forUseAtConfigurationTime()
        }
    }
}
