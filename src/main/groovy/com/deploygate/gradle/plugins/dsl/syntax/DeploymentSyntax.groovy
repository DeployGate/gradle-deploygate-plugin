package com.deploygate.gradle.plugins.dsl.syntax

import com.deploygate.gradle.plugins.dsl.Distribution
import org.gradle.api.Action
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

interface DeploymentSyntax {
    /**
     * Specify a target file explicitly. Basically, this parameter will be set via the build task.
     *
     * @param file an file object of an apk/aab file to upload
     */
    void setSourceFile(@Nullable File file)

    /**
     * A short description of a new revision.
     *
     * @param message
     */
    void setMessage(@Nullable String message)

    /**
     * @deprecated this parameter is no-op.
     */
    void setVisibility(@Nullable String visibility)

    /**
     * Skip inferring and relying on assemble tasks that will be executed before the upload processing.
     *
     * @param skipAssemble Specify true to disable relying on assemble tasks. false by default.
     */
    void setSkipAssemble(boolean skipAssemble)

    /**
     * Configure parameters of a distribution
     *
     * @param closure
     */
    void distribution(@NotNull Closure closure) // for Groovy

    /**
     * Configure parameters of a distribution
     *
     * @param builder
     */
    void distribution(@NotNull Action<Distribution> builder) // for Kotlin DSL
}
