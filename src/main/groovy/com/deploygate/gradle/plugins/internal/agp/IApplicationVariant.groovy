package com.deploygate.gradle.plugins.internal.agp

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.annotations.NotNull

interface IApplicationVariant {

    @NotNull
    String getName()

    /**
     * @return TaskProvider of com.android.build.gradle.tasks.PackageApplication
     */
    @NotNull
    TaskProvider<? extends Task> packageApplicationTaskProvider()
}
