package com.deploygate.gradle.plugins

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.Project
import java.util.HashMap
import org.json.JSONObject

class DeployGateUserUploadTask extends DeployGateTask {
    String apkName
    @TaskAction
    def userUploadTasks() {
        List<Apk> apks = Apk.getApks(project, apkName)
        super.upload(project, apks)
    }
}
