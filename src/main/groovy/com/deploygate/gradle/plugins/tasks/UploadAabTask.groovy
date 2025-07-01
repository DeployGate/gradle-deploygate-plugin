package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.AabInfo
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.VisibleForTesting

abstract class UploadAabTask extends UploadArtifactTask {
    @NotNull
    @VisibleForTesting
    static InputParams createInputParams(@NotNull AabInfo aab, @NotNull DeploymentConfiguration deployment, @NotNull Provider<File> artifactFileProvider) {
        return new InputParams(
                aab.variantName,
                false,
                false,
                deployment.sourceFilePath.getOrElse(aab.aabFile?.absolutePath),
                deployment.message.getOrNull(),
                deployment.distributionKey.getOrNull(),
                deployment.distributionReleaseNote.getOrNull(),
                artifactFileProvider
                )
    }

    @Internal
    final Property<AabInfo> aabInfo

    @Inject
    UploadAabTask(@NotNull ObjectFactory objectFactory, @NotNull ProjectLayout projectLayout) {
        super(objectFactory, projectLayout)
        aabInfo = objectFactory.property(AabInfo)
    }

    @Internal
    @Override
    Provider<InputParams> getInputParamsProvider() {
        return aabInfo.map { aab -> 
            def artifactFileProvider = deployment.sourceFilePath.map { path ->
                def f = new File(path ?: aab.aabFile?.absolutePath)
                f.exists() ? f : null
            }
            createInputParams(aab, deployment, artifactFileProvider) 
        }
    }

    @TaskAction
    void execute() {
        def inputParams = inputParamsProvider.get()

        doUpload(inputParams)
    }

    @Internal
    @Override
    String getDescription() {
        return "Deploy bundled ${inputParamsProvider.map { it.variantName }.getOrElse("variant")} to DeployGate"
    }
}
