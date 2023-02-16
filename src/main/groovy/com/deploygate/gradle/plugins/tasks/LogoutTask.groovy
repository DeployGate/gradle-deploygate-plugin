package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class LogoutTask extends DefaultTask {

    @Internal
    CliCredentialStore credentialStore

    @TaskAction
    def remove() {
        credentialStore.delete()
        logger.info("The local credentials have been removed.")
    }
}
