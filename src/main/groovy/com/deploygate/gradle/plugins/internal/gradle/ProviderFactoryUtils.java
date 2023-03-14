package com.deploygate.gradle.plugins.internal.gradle;

import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

public final class ProviderFactoryUtils {
    private ProviderFactoryUtils() {}

    /**
     * The new provider will return the first-seen value in providers.
     *
     * @param providers
     * @param <T> Value type
     * @return the first-seen value
     */
    public static <T> Provider<T> pickFirst(@NotNull Provider<T>... providers) {
        Provider<T> result = null;

        for (Provider<T> provider : providers) {
            if (result == null) {
                result = provider;
            } else {
                result = result.orElse(provider);
            }
        }

        return result;
    }
}
