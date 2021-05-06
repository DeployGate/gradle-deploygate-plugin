package com.deploygate.gradle.plugins.internal.agp

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

import javax.annotation.Nonnull

interface IApplicationVariant {

    @Nonnull
    String getName()

    /**
     * @return TaskProvider of com.android.build.gradle.tasks.PackageApplication
     */
    @Nonnull
    TaskProvider<? extends Task> packageApplicationTaskProvider()
}
