package com.deploygate.gradle.plugins.tasks.factory

import javax.annotation.Nonnull

interface UploadApkTaskFactory<T> {
    void registerUploadApkTask(@Nonnull T variantOrVariantNameOrCustomName, Object... dependsOn)

    void registerAggregatedUploadApkTask(Object... dependsOn)
}
