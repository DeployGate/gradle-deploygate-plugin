package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.internal.gradle.SingleTask
import com.deploygate.gradle.plugins.internal.gradle.TaskProvider
import org.gradle.api.Task

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
    LazyConfigurableTask lazyPackageApplication() {
        if (AndroidGradlePlugin.taskProviderBased) {
            return new TaskProvider(applicationVariant.packageApplicationProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.packageApplication as Task)
        }
    }
}
