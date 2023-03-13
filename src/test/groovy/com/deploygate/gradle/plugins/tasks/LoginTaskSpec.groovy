package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull
import javax.inject.Inject

class LoginTaskSpec extends Specification {
    static abstract class StubLoginTask extends LoginTask {
        @Internal
        String stubAppOwnerName
        @Internal
        String stubApiToken

        @Inject
        StubLoginTask(@NotNull ObjectFactory objectFactory) {
            super(objectFactory)
        }

        @Override
        boolean setupCredential() {
            deployGateExtension.credentialStore.name = stubAppOwnerName
            deployGateExtension.credentialStore.token = stubApiToken

            return deployGateExtension.credentialStore.name && deployGateExtension.credentialStore.token
        }
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "complete the credentials from cli credentials"() {
        setup:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        def extension = new DeployGateExtension(project, deployments, new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", extension)

        when:
        def loginTask1 = project.tasks.create("loginTask1", StubLoginTask)
        loginTask1.deployGateExtension = extension

        and:
        loginTask1.setup()

        then: "no credentials are found"
        thrown(RuntimeException)

        when:
        def loginTask2 = project.tasks.create("loginTask2", StubLoginTask)
        loginTask2.deployGateExtension = extension

        and:
        loginTask2.stubApiToken = "stub.token"
        loginTask2.stubAppOwnerName = "stub.appOwnerName"
        loginTask2.setup()

        then: "load from the cli credential store"
        loginTask2.credentials.getApiToken().get() == "stub.token"
        loginTask2.credentials.getAppOwnerName().get() == "stub.appOwnerName"

        when:
        def loginTask3 = project.tasks.create("loginTask3", StubLoginTask)
        loginTask3.deployGateExtension = extension

        and:
        loginTask3.credentials.apiToken.set("ext.token")

        and:
        loginTask3.stubApiToken = "stub.token"
        loginTask3.stubAppOwnerName = "stub.appOwnerName"
        loginTask3.setup()

        then: "complete and load by using the extension and the cli credential store"
        loginTask3.credentials.getApiToken().get() == "ext.token"
        loginTask3.credentials.getAppOwnerName().get() == "stub.appOwnerName"

        when:
        def loginTask4 = project.tasks.create("loginTask4", StubLoginTask)
        loginTask4.deployGateExtension = extension

        and:
        loginTask4.credentials.apiToken.set("ext.token")
        loginTask4.credentials.appOwnerName.set("ext.appOwnerName")

        and:
        loginTask4.stubApiToken = "stub.token"
        loginTask4.stubAppOwnerName = "stub.appOwnerName"
        loginTask4.setup()

        then: "do not use the credential store's values"
        loginTask4.credentials.getApiToken().get() == "ext.token"
        loginTask4.credentials.getAppOwnerName().get() == "ext.appOwnerName"
    }
}
