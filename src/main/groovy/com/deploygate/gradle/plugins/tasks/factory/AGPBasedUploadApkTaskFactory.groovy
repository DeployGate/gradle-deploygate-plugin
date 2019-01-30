package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.DeployTarget
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.internal.gradle.SingleTask
import com.deploygate.gradle.plugins.internal.gradle.TaskProvider
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.Project
import org.gradle.api.Task

class AGPBasedUploadApkTaskFactory extends UploadApkTaskFactory {
//    class AGPBasedUploadApkTaskFactory extends UploadApkTaskFactory<com.android.build.gradle.api.ApplicationVariant> {
    AGPBasedUploadApkTaskFactory(Project project) {
        super(project)
    }

//    void registerVariantAwareUploadApkTask(com.android.build.gradle.api.ApplicationVariant applicationVariant, Object... dependsOn) {
    @Override
    void registerUploadApkTask(applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantName), UploadApkTask)

        final DeployTarget deployTarget = deployGateExtension.findDeployTarget(variantName)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantName

            if (deployTarget?.noAssemble) {
                dgTask.dependsOn(dependsOn)
            } else {
                dgTask.dependsOn([androidAssembleTaskName(variantName), *dependsOn].flatten())
            }
        }

        lazyPackageApplication(applicationVariant).configure { packageAppTask ->
            def apkInfo = PackageAppTaskCompat.getApkInfo(packageAppTask)
            def configuration = UploadApkTask.createConfiguration(deployTarget, apkInfo)

            lazyUploadApkTask.configure { dgTask ->
                dgTask.configuration = configuration
                dgTask.applyTaskProfile()
            }
        }
    }

//    private static LazyConfigurableTask<com.android.build.gradle.tasks.PackageApplication> lazyPackageApplication(com.android.build.gradle.api.ApplicationVariant applicationVariant) {
    private static LazyConfigurableTask lazyPackageApplication(applicationVariant) {
        if (AndroidGradlePlugin.taskProviderBased) {
            return new TaskProvider(applicationVariant.packageApplicationProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.packageApplication as Task)
        }
    }
}
