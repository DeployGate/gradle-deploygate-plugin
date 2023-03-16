package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LoginTaskSpec extends Specification {
    static abstract class StubLoginTask extends LoginTask {
        @Internal
        String stubAppOwnerName
        @Internal
        String stubApiToken

        @Inject
        StubLoginTask(@NotNull ObjectFactory objectFactory, @NotNull ProviderFactory providerFactory) {
            super(objectFactory, providerFactory)
        }

        @Override
        boolean setupCredential() {
            CliCredentialStore store = new CliCredentialStore(getCredentialsDirPath().map { new File(it) }.get())

            store.name = stubAppOwnerName
            store.token = stubApiToken
            store.save()

            return store.name && store.token
        }
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "complete the credentials from cli credentials"() {
        setup:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        DeployGateExtension extension = new DeployGateExtension(deployments)
        project.extensions.add("deploygate", extension)

        when:
        project.tasks.register("loginTask1", StubLoginTask) { task ->
            task.credentialsDirPath.set(testProjectDir.root.absolutePath)
        }

        and:
        StubLoginTask loginTask1 = project.tasks.getByName("loginTask1") as StubLoginTask
        loginTask1.execute()

        then: "no credentials are found"
        thrown(RuntimeException)

        when:
        project.tasks.register("loginTask2", StubLoginTask) { task ->
            task.credentialsDirPath.set(testProjectDir.root.absolutePath)
            task.stubApiToken = "stub.token"
            task.stubAppOwnerName = "stub.appOwnerName"
        }

        and:
        StubLoginTask loginTask2 = project.tasks.getByName("loginTask2") as StubLoginTask
        loginTask2.execute()

        then: "load from the cli credential store"
        loginTask2.credentials.getApiToken().get() == "stub.token"
        loginTask2.credentials.getAppOwnerName().get() == "stub.appOwnerName"

        when:
        project.tasks.register("loginTask3", StubLoginTask) { task ->
            task.explicitApiToken.set("ext.token")
            task.credentialsDirPath.set(testProjectDir.root.absolutePath)
            task.stubApiToken = "stub.token"
            task.stubAppOwnerName = "stub.appOwnerName"
        }

        and:
        StubLoginTask loginTask3 = project.tasks.getByName("loginTask3") as StubLoginTask
        loginTask3.execute()

        then: "complete and load by using the extension and the cli credential store"
        loginTask3.credentials.getApiToken().get() == "ext.token"
        loginTask3.credentials.getAppOwnerName().get() == "stub.appOwnerName"

        when:
        project.tasks.register("loginTask4", StubLoginTask) { task ->
            task.explicitApiToken.set("ext.token")
            task.explicitAppOwnerName.set("ext.appOwnerName")
            task.credentialsDirPath.set(testProjectDir.root.absolutePath)
            task.stubApiToken = "stub.token"
            task.stubAppOwnerName = "stub.appOwnerName"
        }

        and:
        StubLoginTask loginTask4 = project.tasks.getByName("loginTask4") as StubLoginTask
        loginTask4.execute()

        then: "do not use the credential store's values"
        loginTask4.credentials.getApiToken().get() == "ext.token"
        loginTask4.credentials.getAppOwnerName().get() == "ext.appOwnerName"
    }
}
