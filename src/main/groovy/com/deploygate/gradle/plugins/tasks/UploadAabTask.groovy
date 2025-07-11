package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.AabInfo
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.VisibleForTesting

abstract class UploadAabTask extends UploadArtifactTask {
    @NotNull
    @VisibleForTesting
    static InputParams createInputParams(@NotNull AabInfo aab, @NotNull DeploymentConfiguration deployment) {
        return new InputParams(
                variantName: aab.variantName,
                artifactFilePath: deployment.sourceFilePath.getOrElse(aab.aabFile?.absolutePath),
                isSigningReady: false,
                isUniversalApk: false,
                message: deployment.message.getOrNull(),
                distributionKey: deployment.distributionKey.getOrNull(),
                releaseNote: deployment.distributionReleaseNote.getOrNull()
                )
    }

    @Nested
    final Property<AabInfo> aabInfo

    @Inject
    UploadAabTask(@NotNull ObjectFactory objectFactory, @NotNull ProjectLayout projectLayout) {
        super(objectFactory, projectLayout)
        aabInfo = objectFactory.property(AabInfo)
    }

    @Internal
    @Override
    Provider<InputParams> getInputParamsProvider() {
        return aabInfo.map { aab -> createInputParams(aab, deployment) }
    }

    @TaskAction
    void execute() {
        def inputParams = inputParamsProvider.get()

        doUpload(inputParams)
    }
}
