package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class UploadApkTaskSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        GradleCompat.init(project)
    }

    def "doUpload should reject unsigned apk"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(NamedDeployment), new CliCredentialStore(File.createTempDir()))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)
        task.deployment.sourceFilePath.set(new File(project.buildDir, "not_found").absolutePath)

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
        task.deployment.sourceFilePath.set(new File(project.buildDir, "not_found").absolutePath)

        when: "universal apk is required"
        task.apkInfo.set(new DirectApkInfo("dep1", null, true, false))

        and:
        task.execute()

        then:
        thrown(IllegalStateException)
    }
}
