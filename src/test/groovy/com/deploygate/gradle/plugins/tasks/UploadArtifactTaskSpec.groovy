package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class UploadArtifactTaskSpec extends Specification {
    static class UploadArtifactTaskStub extends UploadArtifactTask {

        @Override
        void applyTaskProfile() {

        }

        @Override
        void runArtifactSpecificVerification() {

        }
    }

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "getApiToken should get from an extension"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadArtifactTaskStub", UploadArtifactTaskStub)

        and:
        deploygate.apiToken = null

        when:
        task.apiToken

        then:
        thrown(GradleException)

        when:
        deploygate.apiToken = "  "

        and:
        task.apiToken

        then:
        thrown(GradleException)

        when:
        deploygate.apiToken = "token"

        and:
        def token = task.apiToken

        then:
        token == "token"

        when:
        deploygate.apiToken = " token2  "

        and:
        def token2 = task.apiToken

        then:
        token2 == "token2"
    }

    def "getAppOwnerName should get from an extension"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadArtifactTaskStub", UploadArtifactTaskStub)

        and:
        deploygate.appOwnerName = null

        when:
        task.appOwnerName

        then:
        thrown(GradleException)

        when:
        deploygate.appOwnerName = "  "

        and:
        task.appOwnerName

        then:
        thrown(GradleException)

        when:
        deploygate.appOwnerName = "appOwnerName"

        and:
        def token = task.appOwnerName

        then:
        token == "appOwnerName"

        when:
        deploygate.appOwnerName = " appOwnerName2  "

        and:
        def token2 = task.appOwnerName

        then:
        token2 == "appOwnerName2"
    }

    def "setVariantName cannot be called with different names"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadArtifactTaskStub", UploadArtifactTaskStub)

        when:
        task.variantName = "dep1"

        then:
        task.variantName == "dep1"

        when: "try to change the variant name"
        task.variantName = "dep2"

        then: "but the change was ignored"
        thrown(IllegalStateException)
        task.variantName == "dep1"

        when:
        task.variantName = "dep1"

        then: "no exception was thrown"
        noExceptionThrown()
        task.variantName == "dep1"
    }

    def "doUpload should reject illegal states before processing"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadArtifactTaskStub", UploadArtifactTaskStub)
        task.variantName = "dep1"

        when: "apkFile is required"
        task.configuration = new UploadArtifactTask.Configuration(artifactFile: null, isSigningReady: true)

        and:
        task.doUpload()

        then:
        thrown(IllegalStateException)

        when: "apkFile must exist"
        task.configuration = new UploadArtifactTask.Configuration(artifactFile: new File("not found"), isSigningReady: true)

        and:
        task.doUpload()

        then:
        thrown(IllegalStateException)
    }
}
