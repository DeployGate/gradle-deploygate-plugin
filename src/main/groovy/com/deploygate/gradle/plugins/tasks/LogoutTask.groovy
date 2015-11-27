package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LogoutTask extends DefaultTask {
    @TaskAction
    def remove() {
        new CliCredentialStore().delete()
    }
}
