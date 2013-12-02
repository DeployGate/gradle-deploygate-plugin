package com.deploygate.gradle.plugins

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.Project
import java.util.HashMap
import org.json.JSONObject

class DeployGateAllUploadTask extends DeployGateTask {
    @TaskAction
    def uploadDeployGate() {
        List<Apk> apks = Apk.getApks(project)
        super.upload(project, apks)
    }
}
