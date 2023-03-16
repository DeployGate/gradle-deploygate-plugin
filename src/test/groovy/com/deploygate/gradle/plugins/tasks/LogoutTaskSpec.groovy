package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LogoutTaskSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "complete the credentials from cli credentials"() {
        setup:
        project.tasks.register("logoutTask1", LogoutTask) { task ->
            task.credentialsDirPath.set(null)
        }

        when:
        LogoutTask logoutTask1 = project.tasks.getByName("logoutTask1") as LogoutTask
        logoutTask1.remove()

        then: "removed holds false value"
        !logoutTask1.removed.get()

        when:
        def credentialsDirPathProvider = project.providers.provider { testProjectDir.root.absolutePath }
        project.tasks.register("logoutTask2", LogoutTask) { task ->
            task.credentialsDirPath.set(credentialsDirPathProvider)
        }

        and:
        LogoutTask logoutTask2 = project.tasks.getByName("logoutTask2") as LogoutTask
        logoutTask2.remove()

        then: "removed holds true value"
        logoutTask2.removed.get()

        when:
        CliCredentialStore store = new CliCredentialStore(new File(credentialsDirPathProvider.get()))
        store.name = "name"
        store.token = "token"
        store.save()

        and:
        project.tasks.register("logoutTask3", LogoutTask) { task ->
            task.credentialsDirPath.set(credentialsDirPathProvider)
        }

        and:
        LogoutTask logoutTask3 = project.tasks.getByName("logoutTask3") as LogoutTask
        logoutTask3.remove()

        then: "removed holds true value"
        logoutTask3.removed.get()
    }
}
