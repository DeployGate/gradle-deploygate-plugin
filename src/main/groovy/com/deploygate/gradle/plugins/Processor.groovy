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
    private final LogoutTaskFactory logoutTaskFactory

    @Nonnull
    private final UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private final UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadAabTaskFactory

    @Nonnull
    private final UploadArtifactTaskFactory<String> stringBasedUploadApkTaskFactory

    @Nonnull
    private final UploadArtifactTaskFactory<String> stringBasedUploadAabTaskFactory

    def declaredNames = new HashSet<String>()

    Processor(@Nonnull Project project) {
        this(
                project,
                new LogoutTaskFactoryImpl(project),
                new AGPBasedUploadApkTaskFactory(project),
                new AGPBasedUploadAabTaskFactory(project),
                new DSLBasedUploadApkTaskFactory(project),
                new DSLBasedUploadAabTaskFactory(project)
        )
    }

    @VisibleForTesting
    Processor(
            @Nonnull Project project,
            @Nonnull LogoutTaskFactory logoutTaskFactory,
            @Nonnull UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory,
            @Nonnull UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadAabTaskFactory,
            @Nonnull UploadArtifactTaskFactory<String> stringBasedUploadApkTaskFactory,
            @Nonnull UploadArtifactTaskFactory<String> stringBasedUploadAabTaskFactory
    ) {
        this.project = project
        this.logoutTaskFactory = logoutTaskFactory
        this.applicationVariantBasedUploadApkTaskFactory = applicationVariantBasedUploadApkTaskFactory
        this.applicationVariantBasedUploadAabTaskFactory = applicationVariantBasedUploadAabTaskFactory
        this.stringBasedUploadApkTaskFactory = stringBasedUploadApkTaskFactory
        this.stringBasedUploadAabTaskFactory = stringBasedUploadAabTaskFactory
    }

    boolean canProcessVariantAware() {
        return AndroidGradlePlugin.isApplied(project)
    }

    def addVariantOrCustomName(@Nonnull String variantOrCustomName) {
        if (variantOrCustomName) {
            project.logger.debug("${variantOrCustomName} is declared")
            declaredNames.add(variantOrCustomName)
        } else {
            project.logger.warn("the given argument was empty")
        }
    }

    def registerLogoutTask() {
        logoutTaskFactory.registerLogoutTask()
    }

    def registerDeclarationAwareUploadApkTask(String variantOrCustomName) {
        stringBasedUploadApkTaskFactory.registerUploadArtifactTask(variantOrCustomName, *dependencyAncestorOfUploadTaskNames)
    }

    def registerDeclarationAwareUploadAabTask(String variantOrCustomName) {
        stringBasedUploadAabTaskFactory.registerUploadArtifactTask(variantOrCustomName, *dependencyAncestorOfUploadTaskNames)
    }

    def registerAggregatedDeclarationAwareUploadApkTask(Collection<String> variantOrCustomNames) {
        stringBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask(variantOrCustomNames.collect {
            DeployGateTaskFactory.uploadApkTaskName(it)
        })
    }

    def registerAggregatedDeclarationAwareUploadAabTask(Collection<String> variantOrCustomNames) {
        stringBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask(variantOrCustomNames.collect {
            DeployGateTaskFactory.uploadApkTaskName(it)
        })
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
