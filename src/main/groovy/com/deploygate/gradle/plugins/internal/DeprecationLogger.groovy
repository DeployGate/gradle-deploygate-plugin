package com.deploygate.gradle.plugins.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import javax.annotation.Nonnull
import javax.annotation.Nullable

final class DeprecationLogger {
    private final static LOCK = new Object();
    private static DeprecationLogger sInstance;

    private static DeprecationLogger getLogger() {
        if (sInstance) {
            return sInstance
        }

        synchronized (LOCK) {
            if (sInstance) {
                return sInstance;
            }

            sInstance = new DeprecationLogger()
        }

        return sInstance
    }

    static void reset() {
        getLogger().clear()
    }

    static void deprecation(@Nonnull String signature, @Nonnull String sinceVersion, @Nullable String versionToRemove, @Nonnull String message, Object... args) {
        DeprecationLogger deprecationLogger = getLogger();

        if (!deprecationLogger.addSignature(signature)) {
            return
        }

        def notice = "[DeployGate plugin] ${signature} has been deprecated since ${sinceVersion} so we do not recommend to continue using this API."

        if (versionToRemove) {
            notice += " This will be removed from ${versionToRemove}."
        }

        deprecationLogger.log(LogLevel.WARN, notice + " " + message, args)
    }

    private final Logger logger
    private final Set<String> signatureSet

    private DeprecationLogger() {
        this.logger = Logging.getLogger(DeprecationLogger)
        this.signatureSet = new HashSet<>()
    }

    boolean addSignature(String signature) {
        synchronized (signatureSet) {
            return signatureSet.add(signature)
        }
    }

    void log(LogLevel level, String message, Object... args) {
        if (!logger.isEnabled(level)) {
            return;
        }

        if (args) {
            logger.log(level, message, args)
        } else {
            logger.log(level, message)
        }
    }

    void clear() {
        synchronized (signatureSet) {
            signatureSet.clear()
        }
    }
}
