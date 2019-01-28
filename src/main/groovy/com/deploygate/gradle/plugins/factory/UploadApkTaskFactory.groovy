package com.deploygate.gradle.plugins.factory

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.PackageApplication
import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.internal.gradle.SingleTask
import com.deploygate.gradle.plugins.internal.gradle.TaskProvider
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import com.deploygate.gradle.plugins.tasks.UploadTask
import org.gradle.api.Project
import org.gradle.api.Task

class UploadApkTaskFactory extends DeployGateTaskFactory {

    static void onAGPApplied(Project project) {
        def uploadApkTaskFactory = new UploadApkTaskFactory(project)
    }

    private static String uploadApkTaskName(String flavorName) {
        return "uploadDeployGate${flavorName.capitalize()}"
    }

    UploadApkTaskFactory(Project project) {
        super(project)
    }

    void createUploadApkTask(ApplicationVariant applicationVariant, Task... dependsOn) {
        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(applicationVariant.name), UploadApkTask)

        lazyUploadApkTask.configure {
            it.variantName = applicationVariant.name
        }

        final DeployTarget deployTarget = deployGateExtension.apks.findByName(applicationVariant.name)

        project.tasks.register()

        if (!deployTarget.noAssemble) {
            lazyAssemble(applicationVariant).configure { assembleTask ->
                lazyUploadApkTask.configure { dgTask ->
                    dgTask.dependsOn([assembleTask, *dependsOn].flatten())
                }
            }
        } else {
            lazyUploadApkTask.configure { dgTask ->
                dgTask.dependsOn(dependsOn)
            }
        }

        lazyPackageApplication(applicationVariant).configure { packageAppTask ->
            lazyUploadApkTask.configure { dgTask ->
                def isUniversal = packageAppTask.apkNames.size() == 1
                def isSigingReady = !packageAppTask.signingConfig.isEmpty()

                def apkInfo = new DirectApkInfo(
                        applicationVariant.name,
                        new File(packageAppTask.outputDirectory, packageAppTask.apkNames.first()),
                        isSigingReady,
                        isUniversal,
                )

                def configuration = new UploadApkTask.Configuration(
                        apkInfo: apkInfo,
                        deployTarget: deployTarget
                )

                dgTask.configuration = configuration
            }
        }
    }

    private static LazyConfigurableTask<PackageApplication> lazyPackageApplication(ApplicationVariant applicationVariant) {
        if (applicationVariant.hasProperty("packageApplicationProvider")) {
            return new TaskProvider(applicationVariant.packageApplicationProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.packageApplication)
        }
    }

    private static LazyConfigurableTask<PackageApplication> lazyAssemble(ApplicationVariant applicationVariant) {
        if (applicationVariant.hasProperty("assembleProvider")) {
            return new TaskProvider(applicationVariant.assembleProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.assemble)
        }
    }
}
