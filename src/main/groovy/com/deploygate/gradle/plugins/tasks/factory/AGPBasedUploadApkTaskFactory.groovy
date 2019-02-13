package com.deploygate.gradle.plugins.tasks.factory

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.PackageApplication
import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.internal.gradle.SingleTask
import com.deploygate.gradle.plugins.internal.gradle.TaskProvider
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.Project
import org.gradle.api.Task

import javax.annotation.Nonnull

class AGPBasedUploadApkTaskFactory extends UploadApkTaskFactory<ApplicationVariant> {
    AGPBasedUploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadApkTask(@Nonnull ApplicationVariant applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantName), UploadApkTask)

        final VariantBasedDeployTarget deployTarget = deployGateExtension.findDeployTarget(variantName)

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

    @Nonnull
    private static LazyConfigurableTask<PackageApplication> lazyPackageApplication(@Nonnull ApplicationVariant applicationVariant) {
        if (AndroidGradlePlugin.taskProviderBased) {
            return new TaskProvider(applicationVariant.packageApplicationProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.packageApplication as Task)
        }
    }
}
