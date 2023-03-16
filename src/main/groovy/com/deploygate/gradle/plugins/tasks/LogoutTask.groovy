package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull

abstract class LogoutTask extends DefaultTask {

    @Input
    @Optional
    final Property<String> credentialsDirPath

    @Inject
    LogoutTask(@NotNull ObjectFactory objectFactory) {
        credentialsDirPath = objectFactory.property(String)

        description = "Remove the local persisted credentials."
        group = Constants.TASK_GROUP_NAME
    }

    @TaskAction
    def remove() {
        String dirPath = credentialsDirPath.getOrNull()

        if (dirPath == null) {
            logger.info("A local credential is unavailable.")
            return
        }

        CliCredentialStore store = new CliCredentialStore(new File(dirPath))
        store.delete()
        logger.info("The local credentials have been removed.")
    }
}
