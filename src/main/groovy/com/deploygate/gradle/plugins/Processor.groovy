package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.tasks.factory.*
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
    private final Closure<UploadApkTaskFactory> agpBasedUploadApkTaskFactory

    @Nonnull
    private final DSLBasedUploadApkTaskFactory dslBasedUploadApkTaskFactory

    def declaredNames = new HashSet<String>()

    Processor(@Nonnull DeployGatePlugin deployGatePlugin, @Nonnull Project project) {
        this.deployGatePlugin = deployGatePlugin
        this.project = project
        this.loginTaskFactory = new LoginTaskFactory(project)
        this.logoutTaskFactory = new LogoutTaskFactory(project)
        this.agpBasedUploadApkTaskFactory = { ->
            // Be lazy to avoid touching AGPBasedUploadApkTaskFactory on initialize
            // cuz AGPBasedUploadApkTaskFactory has a dependency on Android DSL
            new AGPBasedUploadApkTaskFactory(project)
        }
        this.dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)
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

    def registerVariantAwareUploadApkTask(@Nonnull /* ApplicationVariant */ variant) {
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
