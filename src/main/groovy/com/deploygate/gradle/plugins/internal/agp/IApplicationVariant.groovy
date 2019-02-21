package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask

import javax.annotation.Nonnull

interface IApplicationVariant {

    @Nonnull
    String getName()

    /**
     * @return LazyConfigurableTask of com.android.build.gradle.tasks.PackageApplication
     */
    @Nonnull
    LazyConfigurableTask lazyPackageApplication()
}
