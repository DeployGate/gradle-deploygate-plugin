package com.deploygate.gradle.plugins.internal.gradle;

import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PropertyUtils {
    private PropertyUtils() {
    }

    public static <T> void setIfAbsent(@NotNull Property<T> property, @Nullable T value) {
        if (value == null || property.isPresent()) {
            return;
        }

        property.set(value);
    }

    @NotNull
    public static Property<String> presence(@NotNull Property<String> property) {
        final String value = (property.getOrNull() != null ? property.get() : "").trim();

        if (value.isEmpty()) {
            property.set((String) null);
        } else {
            property.set(value);
        }

        return property;
    }
}
