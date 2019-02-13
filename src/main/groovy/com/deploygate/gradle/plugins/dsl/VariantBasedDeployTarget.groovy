package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.dsl.syntax.DeployTargetSyntax
import org.gradle.api.Named

import javax.annotation.Nonnull
import javax.annotation.Nullable

class VariantBasedDeployTarget implements Named, DeployTargetSyntax {
    @Nonnull
    private String name

    @Nullable
    File sourceFile

    @Nullable
    String uploadMessage

    @Nullable
    String distributionKey

    @Nullable
    String releaseNote

    @Nullable
    String visibility

    boolean skipAssemble

    VariantBasedDeployTarget(@Nonnull String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }

    // backward compatibility

    @Deprecated
    void setMessage(@Nullable String message) {
        setUploadMessage(message)
    }

    @Deprecated
    void setNoAssemble(boolean noAssemble) {
        setSkipAssemble(noAssemble)
    }

    // end: backward compatibility
}