package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.annotation.Internal
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
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
        final Provider<InputParams> inputParamsProvider

        @Inject
        UploadArtifactTaskStub(@NotNull ObjectFactory objectFactory, @NotNull ProviderFactory providerFactory, @NotNull InputParams inputParams) {
            super(objectFactory)
            this.inputParamsProvider = providerFactory.provider { inputParams }
        }
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
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
        task.doUpload(inputParams)

        then:
        thrown(IllegalStateException)
    }
}
