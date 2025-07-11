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
     * Handle forUseAtConfigurationTime compatibility.
     * This method was removed in Gradle 7.0.
     * We need to check the Gradle version at runtime.
     */
    static <T> Provider<T> forUseAtConfigurationTime(Provider<T> provider) {
        try {
            // Try to call forUseAtConfigurationTime if it exists (Gradle < 7.0)
            return provider.forUseAtConfigurationTime()
        } catch (MissingMethodException e) {
            // Method doesn't exist in Gradle 7.0+, just return the provider
            return provider
        }
    }
}
