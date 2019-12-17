package com.deploygate.gradle.plugins.tasks.factory

import javax.annotation.Nonnull

interface UploadArtifactTaskFactory<T> {
    void registerUploadArtifactTask(@Nonnull T variantOrVariantNameOrCustomName, Object... dependsOn)

    void registerAggregatedUploadArtifactTask(Object... dependsOn)
}
