package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import org.gradle.api.GradleException
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

    def "getApiToken should get from an extension"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(VariantBasedDeployTarget))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)

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
        def deploygate = new DeployGateExtension(project, project.container(VariantBasedDeployTarget))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)

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
        def deploygate = new DeployGateExtension(project, project.container(VariantBasedDeployTarget))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)

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

    def "uploadApkToServer should reject illegal states before processing"() {
        setup:
        def deploygate = new DeployGateExtension(project, project.container(VariantBasedDeployTarget))
        project.extensions.add("deploygate", deploygate)

        and:
        def task = project.tasks.create("UploadApkTask", UploadApkTask)
        task.variantName = "dep1"

        when: "signing is required"
        task.configuration = new UploadApkTask.Configuration(isSigningReady: false)

        and:
        task.uploadApkToServer()

        then:
        thrown(IllegalStateException)

        when: "apkFile is required"
        task.configuration = new UploadApkTask.Configuration(apkFile: null, isSigningReady: true)

        and:
        task.uploadApkToServer()

        then:
        thrown(IllegalStateException)

        when: "apkFile must exist"
        task.configuration = new UploadApkTask.Configuration(apkFile: new File("not found"), isSigningReady: true)

        and:
        task.uploadApkToServer()

        then:
        thrown(IllegalStateException)
    }
}
