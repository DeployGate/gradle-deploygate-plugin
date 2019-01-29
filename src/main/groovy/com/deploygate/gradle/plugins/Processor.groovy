package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.factory.LoginTaskFactory
import com.deploygate.gradle.plugins.factory.LogoutTaskFactory
import com.deploygate.gradle.plugins.factory.UploadApkTaskFactory
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import org.gradle.api.Project

import javax.annotation.Nonnull

class Processor {

    static boolean isProcessable(@Nonnull Project project) {
        return AndroidGradlePlugin.isApplied(project)
    }

    @Nonnull
    private final DeployGatePlugin deployGatePlugin

    @Nonnull
    private final Project project

    @Nonnull
    private final LoginTaskFactory loginTaskFactory

    @Nonnull
    private final LogoutTaskFactory logoutTaskFactory

    @Nonnull
    private final UploadApkTaskFactory uploadApkTaskFactory

    Processor(@Nonnull DeployGatePlugin deployGatePlugin, @Nonnull Project project) {
        this.deployGatePlugin = deployGatePlugin
        this.project = project
        this.loginTaskFactory = new LoginTaskFactory(project)
        this.logoutTaskFactory = new LogoutTaskFactory(project)
        this.uploadApkTaskFactory = new UploadApkTaskFactory(project)
    }

    def registerLoginTask() {
        loginTaskFactory.registerLoginTask()
    }

    def registerLogoutTask() {
        logoutTaskFactory.registerLogoutTask()
    }

    def registerDeclarationAwareUploadApkTask(String variantOrCustomName) {
        uploadApkTaskFactory.registerDeclarationAwareUploadApkTask(variantOrCustomName, dependencyAncestorOfUploadTaskName)
    }

    def registerAggregatedDeclarationAwareUploadApkTask(List<String> variantOrCustomNames) {
        uploadApkTaskFactory.registerAggregatedDeclarationAwareUploadApkTask(variantOrCustomNames.collect {
            UploadApkTaskFactory.uploadApkTaskName(it)
        })
    }

//    def registerVariantAwareUploadApkTask(com.android.build.gradle.api.ApplicationVariant variant) {
    def registerVariantAwareUploadApkTask(variant) {
        uploadApkTaskFactory.registerVariantAwareUploadApkTask(variant, dependencyAncestorOfUploadTaskName)
    }

    private static String getDependencyAncestorOfUploadTaskName() {
        return LoginTaskFactory.TASK_NAME
    }
}
