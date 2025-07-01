package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.ApkInfo
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

abstract class UploadApkTask extends UploadArtifactTask {
    @NotNull
    @VisibleForTesting
    static InputParams createInputParams(@NotNull ApkInfo apk, @NotNull DeploymentConfiguration deployment, @NotNull Provider<File> artifactFileProvider) {
        return new InputParams(
                apk.variantName,
                apk.isSigningReady(),
                apk.isUniversalApk(),
                deployment.sourceFilePath.getOrElse(apk.apkFile?.absolutePath),
                deployment.message.getOrNull(),
                deployment.distributionKey.getOrNull(),
                deployment.distributionReleaseNote.getOrNull(),
                artifactFileProvider
                )
    }

    @Internal
    final Property<ApkInfo> apkInfo

    @Inject
    UploadApkTask(@NotNull ObjectFactory objectFactory, @NotNull ProjectLayout projectLayout) {
        super(objectFactory, projectLayout)
        apkInfo = objectFactory.property(ApkInfo)
    }

    @Internal
    @Override
    Provider<InputParams> getInputParamsProvider() {
        return apkInfo.map { apk -> 
            def artifactFileProvider = deployment.sourceFilePath.map { path ->
                def f = new File(path ?: apk.apkFile?.absolutePath)
                f.exists() ? f : null
            }
            createInputParams(apk, deployment, artifactFileProvider) 
        }
    }

    @Internal
    @Override
    String getDescription() {
        return inputParamsProvider.map { inputParams ->
            if (inputParams.isSigningReady) {
                return "Deploy assembled ${inputParams.variantName} to DeployGate"
            } else {
                // require signing config to build a signed APKs
                return "Deploy assembled ${inputParams.variantName} to DeployGate (requires valid signingConfig setting)"
            }
        }.getOrElse("Deploy assembled variant to DeployGate")
    }

    @TaskAction
    void execute() {
        def inputParams = inputParamsProvider.get()

        if (!inputParams.isSigningReady) {
            throw new IllegalStateException('Cannot upload a build without code signature to DeployGate')
        }

        if (!inputParams.isUniversalApk) {
            throw new IllegalStateException('Cannot upload non-universal apk to DeployGate')
        }

        doUpload(inputParams)
    }
}
