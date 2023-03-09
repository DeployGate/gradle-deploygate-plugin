package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.Constants
import com.deploygate.gradle.plugins.tasks.factory.*
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Project

import javax.annotation.Nonnull

/**
 * A processor class to generate tasks, etc.
 *
 * Do not touch the actual classes from Android DSL directly
 */
class Processor {

    @Nonnull
    private final Project project

    @Nonnull
    private final UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private final UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadAabTaskFactory

    def declaredNames = new HashSet<String>()

    Processor(@Nonnull Project project) {
        this(
                project,
                new AGPBasedUploadApkTaskFactory(project),
                new AGPBasedUploadAabTaskFactory(project)
        )
    }

    @VisibleForTesting
    Processor(
            @Nonnull Project project,
            @Nonnull UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory,
            @Nonnull UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadAabTaskFactory
    ) {
        this.project = project
        this.applicationVariantBasedUploadApkTaskFactory = applicationVariantBasedUploadApkTaskFactory
        this.applicationVariantBasedUploadAabTaskFactory = applicationVariantBasedUploadAabTaskFactory
    }

    boolean canProcessVariantAware() {
        return AndroidGradlePlugin.isApplied(project)
    }

    def registerVariantAwareUploadApkTask(@Nonnull IApplicationVariant variant) {
        if (!canProcessVariantAware()) {
            project.logger.error("android gradle plugin not found but tried to create android-specific tasks. Ignored...")
            return
        }

        applicationVariantBasedUploadApkTaskFactory.registerUploadArtifactTask(variant, *dependencyAncestorOfUploadTaskNames)
    }

    def registerVariantAwareUploadAabTask(@Nonnull IApplicationVariant variant) {
        if (!canProcessVariantAware()) {
            project.logger.error("android gradle plugin not found but tried to create android-specific tasks. Ignored...")
            return
        }

        applicationVariantBasedUploadAabTaskFactory.registerUploadArtifactTask(variant, *dependencyAncestorOfUploadTaskNames)
    }

    @VisibleForTesting
    static String[] getDependencyAncestorOfUploadTaskNames() {
        return [Constants.LOGIN_TASK_NAME]
    }
}
