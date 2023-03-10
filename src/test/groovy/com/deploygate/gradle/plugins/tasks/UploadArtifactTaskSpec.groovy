package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.annotation.Internal
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull
import javax.inject.Inject

class UploadArtifactTaskSpec extends Specification {
    static class UploadArtifactTaskStub extends UploadArtifactTask {
        @Internal
        final InputParams inputParams

        @Inject
        UploadArtifactTaskStub(@NotNull ObjectFactory objectFactory, @NotNull InputParams inputParams) {
            super(objectFactory)
            this.inputParams = inputParams
        }
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "getApiToken should get from credentials"() {
        setup:
        def inputParams = new UploadArtifactTask.InputParams(
                variantName: "dep1",
                artifactFilePath: new File(project.buildDir, "not_found").absolutePath
        )
        def task = project.tasks.create("UploadArtifactTask", UploadArtifactTaskStub, inputParams)

        and:
        task.credentials.set(project.objects.newInstance(Credentials))

        and:
        task.credentials.get().apiToken = null

        when:
        task.apiToken

        then:
        thrown(GradleException)

        when:
        task.credentials.get().apiToken = "  "

        and:
        task.apiToken

        then:
        thrown(GradleException)

        when:
        task.credentials.get().apiToken = "token"

        and:
        def token = task.apiToken

        then:
        token == "token"

        when:
        task.credentials.get().apiToken = " token2  "

        and:
        def token2 = task.apiToken

        then:
        token2 == "token2"
    }

    def "getAppOwnerName should get from credentials"() {
        setup:
        def inputParams = new UploadArtifactTask.InputParams(
                variantName: "dep1",
                artifactFilePath: new File(project.buildDir, "not_found").absolutePath
        )
        def task = project.tasks.create("UploadArtifactTask", UploadArtifactTaskStub, inputParams)

        and:
        task.credentials.set(project.objects.newInstance(Credentials))

        and:
        task.credentials.get().appOwnerName = null

        when:
        task.appOwnerName

        then:
        thrown(GradleException)

        when:
        task.credentials.get().appOwnerName = "  "

        and:
        task.appOwnerName

        then:
        thrown(GradleException)

        when:
        task.credentials.get().appOwnerName = "appOwnerName"

        and:
        def token = task.appOwnerName

        then:
        token == "appOwnerName"

        when:
        task.credentials.get().appOwnerName = " appOwnerName2  "

        and:
        def token2 = task.appOwnerName

        then:
        token2 == "appOwnerName2"
    }

    def "doUpload should reject illegal states before processing"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def inputParams = new UploadArtifactTask.InputParams(
                variantName: "dep1",
                artifactFilePath: new File(project.buildDir, "not_found").absolutePath
        )

        when: "apkFile must exist"
        def task = project.tasks.create("UploadArtifactTaskStub2", UploadArtifactTaskStub, inputParams)

        and:
        task.doUpload()

        then:
        thrown(IllegalStateException)
    }
}
