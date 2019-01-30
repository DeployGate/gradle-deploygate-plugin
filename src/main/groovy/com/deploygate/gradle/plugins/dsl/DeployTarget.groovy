package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.DeployGatePlugin
import org.gradle.api.Named
import org.gradle.api.Project

class DeployTarget implements Named {
    static DeployTarget getDefaultDeployTarget(Project project) {
        File sourceFile = System.getenv(DeployGatePlugin.ENV_NAME_SOURCE_FILE)?.with { project.file(this) }
        String uploadMessage = System.getenv(DeployGatePlugin.ENV_NAME_UPLOAD_MESSAGE)
        String distributionKey = System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY)
        String releaseNote = System.getenv(DeployGatePlugin.ENV_NAME_RELEASE_NOTE)
        String visibility = System.getenv(DeployGatePlugin.ENV_NAME_VISIBILITY)

        return new DeployTarget(
                sourceFile: sourceFile,
                message: uploadMessage,
                distributionKey: distributionKey,
                releaseNote: releaseNote,
                visibility: visibility,
        )
    }

    String name

    File sourceFile
    String message
    String distributionKey
    String releaseNote
    String visibility
    boolean noAssemble

    DeployTarget() {
    }

    DeployTarget(String name) {
        this.name = name
    }
}
