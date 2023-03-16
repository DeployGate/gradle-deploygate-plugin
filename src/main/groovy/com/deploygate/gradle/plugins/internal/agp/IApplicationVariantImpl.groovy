package com.deploygate.gradle.plugins.internal.agp

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.annotations.NotNull

class IApplicationVariantImpl implements IApplicationVariant {
    @NotNull
    //    private com.android.build.gradle.api.ApplicationVariant applicationVariant
    private def applicationVariant

    IApplicationVariantImpl(@NotNull /* ApplicationVariant */ applicationVariant) {
        this.applicationVariant = applicationVariant
    }

    @Override
    @NotNull
    String getName() {
        return applicationVariant.name
    }

    @Override
    @NotNull
    TaskProvider<? extends Task> packageApplicationTaskProvider() {
        return applicationVariant.packageApplicationProvider
    }
}
