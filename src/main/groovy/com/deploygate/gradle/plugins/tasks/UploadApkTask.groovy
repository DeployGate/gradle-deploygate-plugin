package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.ApkInfo
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

abstract class UploadApkTask extends UploadArtifactTask {
    @NotNull
    @VisibleForTesting
    static InputParams createInputParams(@NotNull NamedDeployment deployment, @NotNull ApkInfo apk) {
        return new InputParams(
                variantName: apk.variantName,
                artifactFilePath: (deployment.sourceFile ?: apk.apkFile).absolutePath,
                isSigningReady: apk.isSigningReady(),
                isUniversalApk: apk.isUniversalApk(),
                message: deployment.message,
                distributionKey: deployment.distribution.key,
                releaseNote: deployment.distribution.releaseNote
        )
    }

    @Internal
    Property<ApkInfo> apkInfo

    @Internal
    private Provider<InputParams> inputParamsProvider = deployment.map { d ->
        return createInputParams(d, apkInfo.get())
    }

    @Inject
    UploadApkTask(@NotNull ObjectFactory objectFactory) {
        super(objectFactory)
        apkInfo = objectFactory.property(ApkInfo)
        group = Constants.TASK_GROUP_NAME
    }

    @Nested
    @Override
    InputParams getInputParams() {
        return inputParamsProvider.get()
    }

    @Override
    String getDescription() {
        if (inputParams.isSigningReady) {
            return "Deploy assembled ${inputParams.variantName} to DeployGate"
        } else {
            // require signing config to build a signed APKs
            return "Deploy assembled ${inputParams.variantName} to DeployGate (requires valid signingConfig setting)"
        }
    }

    @TaskAction
    void execute() {
        if (!inputParams.isSigningReady) {
            throw new IllegalStateException('Cannot upload a build without code signature to DeployGate')
        }

        if (!inputParams.isUniversalApk) {
            throw new IllegalStateException('Cannot upload non-universal apk to DeployGate')
        }

        doUpload()
    }
}
