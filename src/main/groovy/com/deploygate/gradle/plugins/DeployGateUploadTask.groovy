package com.deploygate.gradle.plugins

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class DeployGateUploadTask extends DeployGateTask {
    String outputName
    boolean hasSigningConfig
    File defaultSourceFile

    @TaskAction
    def upload() {
        if (!hasSigningConfig) {
            throw new GradleException('Cannot upload a build without code signature to DeployGate')
        }

        ApkTarget target = project.deploygate.apks.findByName(outputName)
        if (!target)
            target = new ApkTarget(outputName)
        if (target.sourceFile == null)
            target.sourceFile = defaultSourceFile

        super.upload(project, target)
    }
}
