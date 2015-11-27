package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.auth.DeployGateLocalCredential
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DeployGateRemoveCredentialTask extends DefaultTask {
    @TaskAction
    def remove() {
        new DeployGateLocalCredential().delete()
    }
}
