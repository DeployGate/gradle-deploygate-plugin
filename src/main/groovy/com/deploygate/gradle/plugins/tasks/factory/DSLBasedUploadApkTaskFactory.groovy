package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.annotation.Nonnull

class DSLBasedUploadApkTaskFactory extends DeployGateTaskFactory implements UploadArtifactTaskFactory<String> {
    DSLBasedUploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadArtifactTask(@Nonnull String variantNameOrCustomName, Object... dependsOn) {
        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantNameOrCustomName), UploadApkTask)

        if (!lazyUploadApkTask) {
            project.logger.debug("It sounds $variantNameOrCustomName's upload apk task has been already registered by me or other factories")
            return
        }

        if (!deployGateExtension.hasDeployment(variantNameOrCustomName)) {
            project.logger.error("No associated deployment to $variantNameOrCustomName has been detected")
            project.logger.error("Please report this problem from https://github.com/DeployGate/gradle-deploygate-plugin/issues")

            throw new GradleException("$variantNameOrCustomName could not be handled by DeployGate plugin")
        }

        final NamedDeployment deployment = deployGateExtension.findDeploymentByName(variantNameOrCustomName)

        if (!deployment.skipAssemble) {
            project.logger.debug("$variantNameOrCustomName required assmble but ignored")
        }

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantNameOrCustomName
            dgTask.dependsOn(dependsOn)
        }

        def apkInfo = new DefaultPresetApkInfo(variantNameOrCustomName)
        def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.configuration = configuration
            dgTask.applyTaskProfile()
        }
    }

    @Override
    void registerAggregatedUploadArtifactTask(Object... dependsOn) {
        if (!dependsOn?.flatten()) {
            project.logger.debug("skipped register aggregation tasks")
            return
        }

        taskFactory.registerOrFindBy(AGGREGATION_APK_TASK_NAME, DefaultTask).configure { dgTask ->
            dgTask.group = GROUP_NAME
            dgTask.dependsOn(dependsOn.flatten())
        }
    }
}
