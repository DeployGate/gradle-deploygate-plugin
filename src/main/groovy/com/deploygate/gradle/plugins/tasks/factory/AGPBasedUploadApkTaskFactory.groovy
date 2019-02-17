package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import com.deploygate.gradle.plugins.internal.agp.ApplicationVariantProxy
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.Project

import javax.annotation.Nonnull

class AGPBasedUploadApkTaskFactory extends UploadApkTaskFactory<ApplicationVariantProxy> {
    AGPBasedUploadApkTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadApkTask(@Nonnull ApplicationVariantProxy applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        def lazyUploadApkTask = taskFactory.register(uploadApkTaskName(variantName), UploadApkTask)

        final VariantBasedDeployTarget deployTarget = deployGateExtension.findDeployTarget(variantName)

        lazyUploadApkTask.configure { dgTask ->
            dgTask.variantName = variantName

            if (deployTarget?.skipAssemble) {
                dgTask.dependsOn(dependsOn)
            } else {
                dgTask.dependsOn([androidAssembleTaskName(variantName), *dependsOn].flatten())
            }
        }

        applicationVariant.lazyPackageApplication().configure { packageAppTask ->
            def apkInfo = PackageAppTaskCompat.getApkInfo(packageAppTask)
            def configuration = UploadApkTask.createConfiguration(deployTarget, apkInfo)

            lazyUploadApkTask.configure { dgTask ->
                dgTask.configuration = configuration
                dgTask.applyTaskProfile()
            }
        }
    }
}
