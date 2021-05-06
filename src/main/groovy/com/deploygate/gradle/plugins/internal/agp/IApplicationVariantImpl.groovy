package com.deploygate.gradle.plugins.internal.agp

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

import javax.annotation.Nonnull

class IApplicationVariantImpl implements IApplicationVariant {
    @Nonnull
//    private com.android.build.gradle.api.ApplicationVariant applicationVariant
    private def applicationVariant

    IApplicationVariantImpl(@Nonnull /* ApplicationVariant */ applicationVariant) {
        this.applicationVariant = applicationVariant
    }

    @Override
    @Nonnull
    String getName() {
        return applicationVariant.name
    }

    @Override
    @Nonnull
    TaskProvider<? extends Task> packageApplicationTaskProvider() {
        return applicationVariant.packageApplicationProvider
    }
}
