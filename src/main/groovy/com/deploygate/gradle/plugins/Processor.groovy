package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.ApplicationVariantProxy
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
    private final DeployGatePlugin deployGatePlugin

    @Nonnull
    private final Project project

    @Nonnull
    private final LoginTaskFactory loginTaskFactory

    @Nonnull
    private final LogoutTaskFactory logoutTaskFactory

    @Nonnull
    private final AGPBasedUploadApkTaskFactory agpBasedUploadApkTaskFactory

    @Nonnull
    private final DSLBasedUploadApkTaskFactory dslBasedUploadApkTaskFactory

    def declaredNames = new HashSet<String>()

    Processor(@Nonnull DeployGatePlugin deployGatePlugin, @Nonnull Project project) {
        this(
                deployGatePlugin,
                project,
                new LoginTaskFactory(project),
                new LogoutTaskFactory(project),
                new AGPBasedUploadApkTaskFactory(project),
                new DSLBasedUploadApkTaskFactory(project)
        )
    }

    @VisibleForTesting
    Processor(
            @Nonnull DeployGatePlugin deployGatePlugin,
            @Nonnull Project project,
            @Nonnull LoginTaskFactory loginTaskFactory,
            @Nonnull LogoutTaskFactory logoutTaskFactory,
            @Nonnull AGPBasedUploadApkTaskFactory agpBasedUploadApkTaskFactory,
            @Nonnull DSLBasedUploadApkTaskFactory dslBasedUploadApkTaskFactory
    ) {
        this.deployGatePlugin = deployGatePlugin
        this.project = project
        this.loginTaskFactory = loginTaskFactory
        this.logoutTaskFactory = logoutTaskFactory
        this.agpBasedUploadApkTaskFactory = agpBasedUploadApkTaskFactory
        this.dslBasedUploadApkTaskFactory = dslBasedUploadApkTaskFactory
    }

    boolean canProcessVariantAware() {
        return AndroidGradlePlugin.isApplied(project)
    }

    def addVariantOrCustomName(@Nonnull String variantOrCustomName) {
        project.logger.debug("${variantOrCustomName} is declared")
        declaredNames.add(variantOrCustomName)
    }

    def registerLoginTask() {
        loginTaskFactory.registerLoginTask()
    }

    def registerLogoutTask() {
        logoutTaskFactory.registerLogoutTask()
    }

    def registerDeclarationAwareUploadApkTask(String variantOrCustomName) {
        dslBasedUploadApkTaskFactory.registerUploadApkTask(variantOrCustomName, dependencyAncestorOfUploadTaskName)
    }

    def registerAggregatedDeclarationAwareUploadApkTask(Collection<String> variantOrCustomNames) {
        dslBasedUploadApkTaskFactory.registerAggregatedUploadApkTask(variantOrCustomNames.collect {
            UploadApkTaskFactory.uploadApkTaskName(it)
        })
    }

    def registerVariantAwareUploadApkTask(@Nonnull ApplicationVariantProxy variant) {
        if (!canProcessVariantAware()) {
            project.logger.error("android gradle plugin not found but tried to create android-specific tasks. Ignored...")
            return
        }

        agpBasedUploadApkTaskFactory.registerUploadApkTask(variant, dependencyAncestorOfUploadTaskName)
    }

    private static String getDependencyAncestorOfUploadTaskName() {
        return LoginTaskFactory.TASK_NAME
    }
}
