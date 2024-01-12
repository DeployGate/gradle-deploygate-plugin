package com.deploygate.gradle.plugins.internal.gradle;

import java.util.Objects;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class ProviderFactoryUtils {
    private ProviderFactoryUtils() {}

    /**
     * The new provider will return the first-seen value in providers.
     *
     * @param providers
     * @param <T> Value type
     * @return the first-seen value
     */
    @NotNull public static <T> Provider<T> pickFirst(@NotNull Provider<T>... providers) {
        if (providers.length < 2) {
            throw new IllegalArgumentException("providers must be 2 or greater");
        }

        Provider<T> result = null;

        for (final Provider<T> provider : providers) {
            Objects.requireNonNull(provider, "null provider is not allowed");

            if (result == null) {
                result = provider;
            } else {
                result = result.orElse(provider);
            }
        }

        return result;
    }

    /**
     * The new provider will return the first-seen value in providers.
     *
     * @param names environment variables in order of pickFirst
     * @return the first-seen value
     */
    @NotNull public static Provider<String> environmentVariable(
            @NotNull ProviderFactory providerFactory, @NotNull String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("environment name must be present");
        }

        Provider<String> result = null;

        for (final String name : names) {
            Objects.requireNonNull(name, "null name is not allowed");

            Provider<String> envProvider =
                    GradleCompat.forUseAtConfigurationTime(
                            providerFactory.environmentVariable(name));

            if (result == null) {
                result = envProvider;
            } else {
                //noinspection unchecked
                result = pickFirst(result, envProvider);
            }
        }

        return GradleCompat.forUseAtConfigurationTime(result);
    }
}
