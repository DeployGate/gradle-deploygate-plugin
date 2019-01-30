package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project

class DSLBasedUploadApkTaskFactory extends UploadApkTaskFactory<String> {
    DSLBasedUploadApkTaskFactory(Project project) {
        super(project)
    }

    @Override
    void registerUploadApkTask(String variantNameOrCustomName, Object... dependsOn) {
        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantNameOrCustomName), UploadApkTask, false)

        if (!lazyUploadApkTask) {
            project.logger.debug("It sounds $variantNameOrCustomName's upload apk task has been already registered by AGP based factory")
            return
        }

        if (!deployGateExtension.hasDeployTarget(variantNameOrCustomName)) {
            project.logger.error("The associated deploy target to $variantNameOrCustomName has not been detected")
            project.logger.error("Please report this problem from https://github.com/DeployGate/gradle-deploygate-plugin/issues")

            throw new GradleException("$variantNameOrCustomName could not be handled by DeployGate plugin")
        }

        final VariantBasedDeployTarget deployTarget = deployGateExtension.findDeployTarget(variantNameOrCustomName)

        if (!deployTarget.noAssemble) {
            project.logger.debug("$variantNameOrCustomName required assmble but ignored")
        }

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantNameOrCustomName
            dgTask.dependsOn(dependsOn)
        }

        def apkInfo = new DefaultPresetApkInfo(variantNameOrCustomName)
        def configuration = UploadApkTask.createConfiguration(deployTarget, apkInfo)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.configuration = configuration
            dgTask.applyTaskProfile()
        }
    }

    void registerAggregatedUploadApkTask(Object... dependsOn) {
        if (!dependsOn?.flatten()) {
            project.logger.debug("skipped register aggregation tasks")
            return
        }

        taskFactory.register(AGGREGATION_TASK_NAME, DefaultTask).configure { dgTask ->
            dgTask.group = GROUP_NAME
            dgTask.dependsOn(dependsOn.flatten())
        }
    }
}
