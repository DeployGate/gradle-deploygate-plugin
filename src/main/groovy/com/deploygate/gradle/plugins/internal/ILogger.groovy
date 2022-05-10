package com.deploygate.gradle.plugins.internal

import javax.annotation.Nonnull
import javax.annotation.Nullable

interface ILogger {

    void error(@Nullable Throwable t, @Nullable String message, Object... args);

    void warning(@Nonnull String message, Object... args);

    void info(@Nonnull String message, Object... args);

    void verbose(@Nonnull String message, Object... args);

    void quiet(@Nonnull String message, Object... args);

    void lifecycle(@Nonnull String message, Object... args);

    void deprecation(@Nonnull String signature, @Nonnull String sinceVersion, @Nullable String versionToRemove, @Nonnull String message, Object... args)
}
