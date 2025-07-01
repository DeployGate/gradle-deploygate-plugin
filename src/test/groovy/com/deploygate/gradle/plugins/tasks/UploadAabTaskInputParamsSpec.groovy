package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectAabInfo
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class UploadAabTaskInputParamsSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        GradleCompat.init(project)
    }

    @Unroll
    def "create a inputParams"() {
        setup:
        def deployment = project.objects.newInstance(DeploymentConfiguration)
        deployment.message.set(message)
        deployment.distributionKey.set(distributionKey)
        deployment.distributionReleaseNote.set(distributionReleaseNote)
        deployment.skipAssemble.set(skipAssemble)

        and:
        def aabInfo = new DirectAabInfo("dep1", aabFile)

        and:
        def artifactFileProvider = project.providers.provider {
            def f = new File(deployment.sourceFilePath.getOrElse(aabInfo.aabFile?.absolutePath))
            f.exists() ? f : null
        }
        def inputParams = UploadAabTask.createInputParams(aabInfo, deployment, artifactFileProvider)

        expect:
        inputParams.message == message
        inputParams.distributionKey == distributionKey
        inputParams.releaseNote == distributionReleaseNote

        where:
        message   | distributionKey   | distributionReleaseNote   | skipAssemble | aabFile
        null      | null              | null                      | false        | new File("build.gradle")
        "message" | "distributionKey" | "distributionReleaseNote" | true         | new File("build.gradle")
    }

    @Unroll
    def "create a inputParams for aab file handling"() {
        setup:
        def deployment = project.objects.newInstance(DeploymentConfiguration)
        deployment.sourceFilePath.set(sourceFile?.absolutePath)

        and:
        def aabInfo = new DirectAabInfo("dep1", aabFile)

        and:
        def artifactFileProvider = project.providers.provider {
            def f = new File(deployment.sourceFilePath.getOrElse(aabInfo.aabFile?.absolutePath))
            f.exists() ? f : null
        }
        def inputParams = UploadAabTask.createInputParams(aabInfo, deployment, artifactFileProvider)

        expect:
        inputParams.artifactFilePath == (sourceFile ?: aabFile).absolutePath

        where:
        sourceFile               | aabFile
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build2.gradle")
    }
}
