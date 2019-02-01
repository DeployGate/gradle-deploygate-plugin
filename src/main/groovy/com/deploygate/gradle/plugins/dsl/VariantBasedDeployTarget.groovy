package com.deploygate.gradle.plugins.dsl

import org.gradle.api.Named

import javax.annotation.Nullable

interface VariantBasedDeployTarget extends Named {

    void setSourceFile(@Nullable File sourceFile)

    @Nullable
    File getSourceFile()

    void setUploadMessage(@Nullable String uploadMessage)

    @Nullable
    String getUploadMessage()

    void setDistributionKey(@Nullable String distributionKey)

    @Nullable
    String getDistributionKey()

    void setReleaseNote(@Nullable String releaseNote)

    @Nullable
    String getReleaseNote()

    void setVisibility(@Nullable String visibility)

    @Nullable
    String getVisibility()

    void setSkipAssemble(boolean skipAssemble)

    boolean isSkipAssemble()
}
