package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.ApkInfo
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import org.gradle.api.tasks.TaskAction

import javax.annotation.Nonnull

class UploadApkTask extends UploadArtifactTask {
    static Configuration createConfiguration(@Nonnull NamedDeployment deployment, @Nonnull ApkInfo apkInfo) {
        return new Configuration(
                artifactFile: deployment.sourceFile ?: apkInfo.apkFile,
                isSigningReady: apkInfo.isSigningReady(),
                isUniversalApk: apkInfo.isUniversalApk(),
                uploadParams: createUploadParams(deployment)
        )
    }

    @TaskAction
    void doUpload() {
        super.doUpload()
    }

    @Override
    void applyTaskProfile() {
        setDescription("Deploy assembled $variantName to DeployGate")

        if (!configuration.isSigningReady) {
            // require signing config to build a signed APKs
            setDescription(description + " (requires valid signingConfig setting)")
        }

        if (configuration.isUniversalApk) {
            group = DeployGateTaskFactory.GROUP_NAME
        }
    }

    @Override
    void runArtifactSpecificVerification() {
        if (!configuration.isSigningReady) {
            throw new IllegalStateException('Cannot upload a build without code signature to DeployGate')
        }
    }
}
