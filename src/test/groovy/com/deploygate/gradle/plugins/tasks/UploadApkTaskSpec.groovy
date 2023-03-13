package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class UploadApkTaskSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "doUpload should reject unsigned apk"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)
        task.deployment.set(new NamedDeployment("dep1").tap { it.sourceFile = new File(project.buildDir, "not_found") })

        when: "signing is required"
        task.apkInfo.set(new DirectApkInfo("dep1", null, false, true))

        and:
        task.execute()

        then:
        thrown(IllegalStateException)
    }

    def "doUpload should reject non universal apk"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)
        task.deployment.set(new NamedDeployment("dep1").tap { it.sourceFile = new File(project.buildDir, "not_found") })

        when: "universal apk is required"
        task.apkInfo.set(new DirectApkInfo("dep1", null, true, false))

        and:
        task.execute()

        then:
        thrown(IllegalStateException)
    }
}
