package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.AabInfo
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import org.gradle.api.tasks.TaskAction

import javax.annotation.Nonnull

class UploadAabTask extends UploadArtifactTask {
    static Configuration createConfiguration(@Nonnull NamedDeployment deployment, @Nonnull AabInfo aabInfo) {
        return new Configuration(
                artifactFile: deployment.sourceFile ?: aabInfo.aabFile,
                isSigningReady: false,
                isUniversalApk: false,
                uploadParams: createUploadParams(deployment)
        )
    }

    @TaskAction
    void doUpload() {
        super.doUpload()
    }

    @Override
    void applyTaskProfile() {
        setDescription("Deploy bundled $variantName to DeployGate")

        group = DeployGateTaskFactory.GROUP_NAME
    }

    @Override
    void runArtifactSpecificVerification() {
    }
}
