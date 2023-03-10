package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.tasks.Constants

import javax.annotation.Nonnull

final class DeployGateTaskFactory {
    public static final String GROUP_NAME = Constants.TASK_GROUP_NAME
    public static String SUFFIX_APK_TASK_NAME = "uploadDeployGate"
    public static String SUFFIX_AAB_TASK_NAME = "uploadDeployGateAab"

    @Nonnull
    static String uploadApkTaskName(@Nonnull String variantName) {
        return "$SUFFIX_APK_TASK_NAME${variantName.capitalize()}"
    }

    @Nonnull
    static String uploadAabTaskName(@Nonnull String variantName) {
        return "$SUFFIX_AAB_TASK_NAME${variantName.capitalize()}"
    }
}
