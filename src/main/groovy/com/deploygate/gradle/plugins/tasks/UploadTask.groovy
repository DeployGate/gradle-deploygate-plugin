package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.entities.DeployTarget
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class UploadTask extends BaseUploadTask {
    String outputName
    boolean hasSigningConfig
    File defaultSourceFile

    @TaskAction
    def upload() {
        if (!hasSigningConfig) {
            throw new GradleException('Cannot upload a build without code signature to DeployGate')
        }

        DeployTarget target = project.deploygate.apks.findByName(outputName)
        if (!target)
            target = new DeployTarget(outputName)
        if (target.sourceFile == null)
            target.sourceFile = defaultSourceFile

        project.deploygate.notifyServer 'start_upload', [ 'length': Long.toString(target.sourceFile?.length()) ]

        def res = super.upload(project, target)

        if (res.error)
            project.deploygate.notifyServer 'upload_finished', [ 'error': true, message: res.message ]
        else
            project.deploygate.notifyServer 'upload_finished', [ 'path': res.results.path ]
    }
}
