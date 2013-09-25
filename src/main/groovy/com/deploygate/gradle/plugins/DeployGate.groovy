package com.deploygate.gradle.plugins

import org.gradle.api.GradleException;
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployGate implements Plugin<Project> {
    void apply(Project project) {
        def deploygate = new DeployGateExtension(project.container(ApkTarget))
        project.convention.plugins.deploygate = deploygate 
        project.extensions.deploygate = deploygate

        def apkUpload = project.task('uploadDeployGate', type: DeployGateTask)
        apkUpload.group = 'DeployGate' 
        apkUpload.description = 'Upload the apk file to deploygate and distribution update'
    }
}
