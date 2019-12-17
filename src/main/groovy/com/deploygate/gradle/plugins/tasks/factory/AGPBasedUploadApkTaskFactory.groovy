package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.Project

import javax.annotation.Nonnull

import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidAssembleTaskName

class AGPBasedUploadApkTaskFactory extends DeployGateTaskFactory implements UploadArtifactTaskFactory<IApplicationVariant> {
    AGPBasedUploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadArtifactTask(@Nonnull IApplicationVariant applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        // depends on other task provider, so we need to get a task right now.
        def dgTask = taskFactory.register(uploadApkTaskName(variantName), UploadApkTask).get()

        final NamedDeployment deployment = deployGateExtension.findDeploymentByName(variantName)

        dgTask.variantName = variantName

        if (deployment?.skipAssemble) {
            dgTask.dependsOn(dependsOn)
        } else {
            dgTask.dependsOn([androidAssembleTaskName(variantName), *dependsOn].flatten())
        }

        applicationVariant.lazyPackageApplication().configure { packageAppTask ->
            def apkInfo = PackageAppTaskCompat.getApkInfo(packageAppTask)
            def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

            dgTask.configuration = configuration
            dgTask.applyTaskProfile()
        }

        if (deployment?.skipAssemble) {
            // a package application provider may not be evaluated in some cases.
            // ref: https://github.com/DeployGate/gradle-deploygate-plugin/issues/86
            applicationVariant.lazyPackageApplication().get()
        }
    }

    @Override
    void registerAggregatedUploadArtifactTask(Object... dependsOn) {
        throw new IllegalAccessException("this method is not allowed to be called")
    }
}
