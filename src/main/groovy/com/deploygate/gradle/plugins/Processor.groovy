package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
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
    private final LoginTaskFactory loginTaskFactory

    @Nonnull
    private final LogoutTaskFactory logoutTaskFactory

    @Nonnull
    private final UploadApkTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private final UploadApkTaskFactory<String> stringBasedUploadApkTaskFactory

    def declaredNames = new HashSet<String>()

    Processor(@Nonnull Project project) {
        this(
                project,
                new LoginTaskFactoryImpl(project),
                new LogoutTaskFactoryImpl(project),
                new AGPBasedUploadApkTaskFactory(project),
                new DSLBasedUploadApkTaskFactory(project)
        )
    }

    @VisibleForTesting
    Processor(
            @Nonnull Project project,
            @Nonnull LoginTaskFactory loginTaskFactory,
            @Nonnull LogoutTaskFactory logoutTaskFactory,
            @Nonnull UploadApkTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory,
            @Nonnull UploadApkTaskFactory<String> stringBasedUploadApkTaskFactory
    ) {
        this.project = project
        this.loginTaskFactory = loginTaskFactory
        this.logoutTaskFactory = logoutTaskFactory
        this.applicationVariantBasedUploadApkTaskFactory = applicationVariantBasedUploadApkTaskFactory
        this.stringBasedUploadApkTaskFactory = stringBasedUploadApkTaskFactory
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

    def registerLoginTask() {
        loginTaskFactory.registerLoginTask()
    }

    def registerLogoutTask() {
        logoutTaskFactory.registerLogoutTask()
    }

    def registerDeclarationAwareUploadApkTask(String variantOrCustomName) {
        stringBasedUploadApkTaskFactory.registerUploadApkTask(variantOrCustomName, *dependencyAncestorOfUploadTaskNames)
    }

    def registerAggregatedDeclarationAwareUploadApkTask(Collection<String> variantOrCustomNames) {
        stringBasedUploadApkTaskFactory.registerAggregatedUploadApkTask(variantOrCustomNames.collect {
            DeployGateTaskFactory.uploadApkTaskName(it)
        })
    }

    def registerVariantAwareUploadApkTask(@Nonnull IApplicationVariant variant) {
        if (!canProcessVariantAware()) {
            project.logger.error("android gradle plugin not found but tried to create android-specific tasks. Ignored...")
            return
        }

        applicationVariantBasedUploadApkTaskFactory.registerUploadApkTask(variant, *dependencyAncestorOfUploadTaskNames)
    }

    @VisibleForTesting
    static String[] getDependencyAncestorOfUploadTaskNames() {
        return [LoginTaskFactory.TASK_NAME]
    }
}
