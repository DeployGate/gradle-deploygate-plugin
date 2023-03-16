package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.NotNull

abstract class LogoutTask extends DefaultTask {

    @Inject
    LogoutTask() {
        description = "Remove the local persisted credentials."
        group = Constants.TASK_GROUP_NAME
    }

    @Input
    @Optional
    @NotNull
    abstract Property<String> getCredentialsDirPath()

    @Internal
    @NotNull
    abstract Property<Boolean> getRemoved()

    @TaskAction
    def remove() {
        String dirPath = credentialsDirPath.getOrNull()

        if (dirPath == null) {
            logger.info("A local credential is unavailable.")
            getRemoved().set(false)
            return
        }

        CliCredentialStore store = new CliCredentialStore(new File(dirPath))

        getRemoved().set(store.delete())

        logger.info("The local credentials have been removed.")
    }
}
