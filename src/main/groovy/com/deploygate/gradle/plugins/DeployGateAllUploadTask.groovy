package com.deploygate.gradle.plugins

import org.gradle.api.tasks.TaskAction

class DeployGateAllUploadTask extends DeployGateTask {
    @TaskAction
    def uploadDeployGate() {
        List<Apk> apks = Apk.getApks(project)
        super.upload(project, apks)
    }
}
