package com.deploygate.gradle.plugins.tasks.factory

import org.gradle.api.Project

import javax.annotation.Nonnull

abstract class UploadApkTaskFactory<T> extends DeployGateTaskFactory {
    static String AGGREGATION_TASK_NAME = "uploadDeployGate"

    @Nonnull
    static String uploadApkTaskName(@Nonnull String variantName) {
        return "$AGGREGATION_TASK_NAME${variantName.capitalize()}"
    }

    @Nonnull
    static String androidAssembleTaskName(@Nonnull String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    UploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    abstract void registerUploadApkTask(@Nonnull T variantOrVariantNameOrCustomName, Object... dependsOn)
}
