package com.deploygate.gradle.plugins.factory

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.PackageApplication
import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.internal.gradle.SingleTask
import com.deploygate.gradle.plugins.internal.gradle.TaskProvider
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project

class UploadApkTaskFactory extends DeployGateTaskFactory {
    private static String AGGREGATION_TASK_NAME = "uploadDeployGate"

    static String uploadApkTaskName(String variantName) {
        return "uploadDeployGate${variantName.capitalize()}"
    }

    private static String androidAssembleTaskName(String variantName) {
        return "assemble${variantName.capitalize()}"
    }

    UploadApkTaskFactory(Project project) {
        super(project)
    }

    void registerVariantAwareUploadApkTask(ApplicationVariant applicationVariant, Object... dependsOn) {
        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(applicationVariant.name), UploadApkTask)

        final DeployTarget deployTarget = deployGateExtension.apks.findByName(applicationVariant.name)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = applicationVariant.name

            if (deployTarget?.noAssemble) {
                dgTask.dependsOn(dependsOn)
            } else {
                dgTask.dependsOn([androidAssembleTaskName(applicationVariant.name), *dependsOn].flatten())
            }
        }

//        if (deployTarget?.noAssemble) {
//            lazyUploadApkTask.configure { dgTask ->
//                dgTask.dependsOn(dependsOn)
//            }
//        } else {
//            lazyAssemble(applicationVariant).configure { assembleTask ->
//                lazyUploadApkTask.configure { dgTask ->
//                    dgTask.dependsOn([assembleTask, *dependsOn].flatten())
//                }
//            }
//        }

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
                dgTask.applyTaskProfile()
            }
        }
    }

    void registerDeclarationAwareUploadApkTask(String variantOrCustomName, Object... dependsOn) {
        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantOrCustomName), UploadApkTask, false)

        if (!lazyUploadApkTask) {
            project.logger.debug("It sounds $variantOrCustomName's upload apk task has been already registered")
            return
        }

        final DeployTarget deployTarget = deployGateExtension.apks.findByName(variantOrCustomName)

        if (!deployTarget) {
            project.logger.error("The associated deploy target to $variantOrCustomName has not been detected")
            project.logger.error("Please report this problem from https://github.com/DeployGate/gradle-deploygate-plugin/issues")

            throw new GradleException("$variantOrCustomName could not be handled by DeployGate plugin")
        }

        if (!deployTarget.noAssemble) {
            project.logger.debug("$variantOrCustomName required assmble but ignored")
        }

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantOrCustomName
            dgTask.dependsOn(dependsOn)
        }

//        lazyUploadApkTask.configure { dgTask ->
//            dgTask.dependsOn(dependsOn)
//        }

        lazyUploadApkTask.configure { dgTask ->
            def apkInfo = new DirectApkInfo(
                    variantOrCustomName,
                    null,
                    true,
                    true,
            )

            def configuration = new UploadApkTask.Configuration(
                    apkInfo: apkInfo,
                    deployTarget: deployTarget
            )

            dgTask.configuration = configuration
            dgTask.applyTaskProfile()
        }
    }

    void registerAggregatedDeclarationAwareUploadApkTask(Object... dependsOn) {
        if (!dependsOn) {
            project.logger.debug("skipped register aggregation tasks")
            return
        }

        taskFactory.register(AGGREGATION_TASK_NAME, DefaultTask).configure { dgTask ->
            dgTask.group = GROUP_NAME
            dgTask.dependsOn(dependsOn)
        }
    }

    private static LazyConfigurableTask<PackageApplication> lazyPackageApplication(ApplicationVariant applicationVariant) {
        if (AndroidGradlePlugin.taskProviderBased) {
            return new TaskProvider(applicationVariant.packageApplicationProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.packageApplication)
        }
    }

    private static LazyConfigurableTask<PackageApplication> lazyAssemble(ApplicationVariant applicationVariant) {
        if (AndroidGradlePlugin.taskProviderBased) {
            return new TaskProvider(applicationVariant.assembleProvider as org.gradle.api.tasks.TaskProvider)
        } else {
            return new SingleTask(applicationVariant.assemble)
        }
    }
}
