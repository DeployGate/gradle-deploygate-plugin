package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.AabInfo
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.annotation.Internal

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.VisibleForTesting

import javax.inject.Inject

abstract class UploadAabTask extends UploadArtifactTask {
    @NotNull
    @VisibleForTesting
    static InputParams createInputParams(@NotNull NamedDeployment deployment, @NotNull AabInfo aab) {
        return new InputParams(
                variantName: aab.variantName,
                artifactFilePath: (deployment.sourceFile ?: aab.aabFile).absolutePath,
                isSigningReady: false,
                isUniversalApk: false,
                message: deployment.message,
                distributionKey: deployment.distribution.key,
                releaseNote: deployment.distribution.releaseNote
        )
    }

    @Internal
    Property<AabInfo> aabInfo

    @Inject
    UploadAabTask(@NotNull ObjectFactory objectFactory) {
        super(objectFactory)
        aabInfo = objectFactory.property(AabInfo)
        group = Constants.TASK_GROUP_NAME
    }

    @Internal
    @Override
    Provider<InputParams> getInputParamsProvider() {
        return deployment.map { d -> createInputParams(d, aabInfo.get()) }
    }

    @TaskAction
    void execute() {
        def inputParams = inputParamsProvider.get()

        doUpload(inputParams)
    }

    @Override
    String getDescription() {
        return "Deploy bundled ${inputParamsProvider.get().variantName} to DeployGate"
    }
}
