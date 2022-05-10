package com.deploygate.gradle.plugins.dsl.syntax

import javax.annotation.Nonnull
import javax.annotation.Nullable

interface DeploymentSyntax {
    void setSourceFile(@Nullable File file)

    void setMessage(@Nullable String message)

    void setVisibility(@Nullable String visibility)

    void setSkipAssemble(boolean skipAssemble)

    void distribution(@Nonnull Closure closure)
}
