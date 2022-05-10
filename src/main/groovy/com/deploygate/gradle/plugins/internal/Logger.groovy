package com.deploygate.gradle.plugins.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging

import javax.annotation.Nonnull
import javax.annotation.Nullable

final class Logger implements ILogger {
    private static final LogLevel ERROR = LogLevel.ERROR;
    private static final LogLevel WARN = LogLevel.WARN;
    private static final LogLevel QUIET = LogLevel.QUIET;
    private static final LogLevel LIFECYCLE = LogLevel.LIFECYCLE;
    private static final LogLevel INFO = LogLevel.INFO;
    private static final LogLevel VERBOSE = LogLevel.INFO;

    static ILogger getLogger(Class klass) {
        return new Logger(Logging.getLogger(klass))
    }

    private final org.gradle.api.logging.Logger logger;

    private Logger(org.gradle.api.logging.Logger logger) {
        this.logger = logger
    }

    @Override
    void error(@Nullable Throwable t, @Nullable String message, Object... args) {
        log(ERROR, message, args)
    }

    @Override
    void warning(@Nonnull String message, Object... args) {
        log(WARN, message, args)
    }

    @Override
    void info(@Nonnull String message, Object... args) {
        log(INFO, message, args)
    }

    @Override
    void verbose(@Nonnull String message, Object... args) {
        log(VERBOSE, message, args)
    }

    @Override
    void quiet(@Nonnull String message, Object... args) {
        log(QUIET, message, args)
    }

    @Override
    void lifecycle(@Nonnull String message, Object... args) {
        log(LIFECYCLE, message, args)
    }

    @Override
    void deprecation(@Nonnull String signature, @Nonnull String sinceVersion, @Nullable String versionToRemove, @Nonnull String message, Object... args) {
        def notice = "${signature} has been deprecated since ${sinceVersion} so we do not recommend to continue using this API."

        if (versionToRemove) {
            notice += " This will be removed from ${versionToRemove}."
        }

        log(WARN, notice + " " + message, args)
    }

    private void log(@Nonnull LogLevel level, @Nonnull String message, Object... args) {
        if (!logger.isEnabled(level)) {
            return;
        }

        if (args) {
            logger.log(level, message, args)
        } else {
            logger.log(level, message)
        }
    }
}
