package com.deploygate.gradle.plugins.dsl.syntax

import javax.annotation.Nullable

interface DeploymentSyntax {
    void setSourceFile(@Nullable File file)

    void setUploadMessage(@Nullable String message)

    void setDistributionKey(@Nullable String distributionKey)

    void setReleaseNote(@Nullable String releaseNote)

    void setVisibility(@Nullable String visibility)

    void setSkipAssemble(boolean skipAssemble)
}
