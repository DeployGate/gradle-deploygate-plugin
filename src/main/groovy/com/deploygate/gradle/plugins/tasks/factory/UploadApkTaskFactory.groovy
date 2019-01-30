package com.deploygate.gradle.plugins.tasks.factory

import org.gradle.api.Project

abstract class UploadApkTaskFactory<T> extends DeployGateTaskFactory {
    static String AGGREGATION_TASK_NAME = "uploadDeployGate"

    static String uploadApkTaskName(String variantName) {
        return "$AGGREGATION_TASK_NAME${variantName.capitalize()}"
    }

    static String androidAssembleTaskName(String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    UploadApkTaskFactory(Project project) {
        super(project)
    }

    abstract void registerUploadApkTask(T variantOrVariantNameOrCustomName, Object... dependsOn)
}
