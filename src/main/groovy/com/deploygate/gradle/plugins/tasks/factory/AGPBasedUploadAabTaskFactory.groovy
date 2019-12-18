package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.DirectAabInfo
import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.UploadAabTask
import org.gradle.api.Project

import javax.annotation.Nonnull

import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidBundleTaskName

class AGPBasedUploadAabTaskFactory extends DeployGateTaskFactory implements UploadArtifactTaskFactory<IApplicationVariant> {
    AGPBasedUploadAabTaskFactory(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerUploadArtifactTask(@Nonnull IApplicationVariant applicationVariant, Object... dependsOn) {
        String variantName = applicationVariant.name

        // depends on other task provider, so we need to get a task right now.
        def dgTask = taskFactory.registerOrFindBy(uploadAabTaskName(variantName), UploadAabTask).get()

        final NamedDeployment deployment = deployGateExtension.findDeploymentByName(variantName)

        dgTask.variantName = variantName

        if (deployment?.skipAssemble) {
            dgTask.dependsOn(dependsOn)
        } else {
            dgTask.dependsOn([androidBundleTaskName(variantName), *dependsOn].flatten())
        }

        dgTask.lazyPackageApplication = applicationVariant.lazyPackageApplication()

        // cannot get an aab name from package application
        def baseName = project.properties["archivesBaseName"] as String
        def aabInfo = new DirectAabInfo(variantName, new File(project.buildDir, "outputs/bundle/${variantName}/${baseName}.aab"))

        dgTask.configuration = UploadAabTask.createConfiguration(deployment, aabInfo)
        dgTask.applyTaskProfile()
    }

    @Override
    void registerAggregatedUploadArtifactTask(Object... dependsOn) {
        throw new IllegalAccessException("this method is not allowed to be called")
    }
}
