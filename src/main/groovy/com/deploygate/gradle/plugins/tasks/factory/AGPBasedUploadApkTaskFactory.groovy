package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.Project

import javax.annotation.Nonnull

import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidAssembleTaskName

class AGPBasedUploadApkTaskFactory extends DeployGateTaskFactory implements UploadApkTaskFactory<IApplicationVariant> {
    AGPBasedUploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadApkTask(@Nonnull IApplicationVariant applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantName), UploadApkTask)

        final NamedDeployment deployment = deployGateExtension.findDeploymentByName(variantName)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantName

            if (deployment?.skipAssemble) {
                dgTask.dependsOn(dependsOn)
            } else {
                dgTask.dependsOn([androidAssembleTaskName(variantName), *dependsOn].flatten())
            }
        }

        applicationVariant.lazyPackageApplication().configure { packageAppTask ->
            def apkInfo = PackageAppTaskCompat.getApkInfo(packageAppTask)
            def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

            lazyUploadApkTask.configure { dgTask ->
                dgTask.configuration = configuration
                dgTask.applyTaskProfile()
            }
        }
    }

    @Override
    void registerAggregatedUploadApkTask(Object... dependsOn) {
        throw new IllegalAccessException("this method is not allowed to be called")
    }
}
