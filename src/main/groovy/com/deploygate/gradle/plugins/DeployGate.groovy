package com.deploygate.gradle.plugins

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployGate implements Plugin<Project> {
    void apply(Project project) {
        def apks = project.container(ApkTarget) {
            String apkName = it.toString()
            def userTask = project.task("${apkName}UploadDeployGate", type: DeployGateUserUploadTask)
            userTask.group = 'DeployGate' 
            userTask.description = "Upload the ${apkName} APK file"
            userTask.apkName = apkName

            project.extensions.create(it, ApkTarget, apkName)
        }

        def deploygate = new DeployGateExtension(apks)
        project.convention.plugins.deploygate = deploygate 
        project.extensions.deploygate = deploygate

        def apkUpload = project.task('uploadDeployGate', type: DeployGateAllUploadTask)
        apkUpload.group = 'DeployGate' 
        apkUpload.description = 'Uploads the APK file. Also updates the distribution specified by distributionKey if configured'
    }
}
