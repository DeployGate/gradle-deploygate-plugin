package com.deploygate.gradle.plugins.internal.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNullOrBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    @Nullable
    public static String capitalize(@Nullable String value) {
        if (isNullOrBlank(value)) {
            return value;
        }

        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }
}
