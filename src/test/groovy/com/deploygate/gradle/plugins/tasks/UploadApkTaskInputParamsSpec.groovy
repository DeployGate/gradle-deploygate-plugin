package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull

class UploadApkTaskInputParamsSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
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
        def apkInfo = new DirectApkInfo("dep1", apkFile, signingReady, universalApk)

        and:
        def inputParams = UploadApkTask.createInputParams(apkInfo, deployment)

        expect:
        inputParams.message == message
        inputParams.distributionKey == distributionKey
        inputParams.releaseNote == distributionReleaseNote
        inputParams.isSigningReady == signingReady
        inputParams.isUniversalApk == universalApk

        where:
        message   | distributionKey   | distributionReleaseNote   | skipAssemble | signingReady | universalApk | apkFile
        null      | null              | null                      | false        | false        | false        | new File("build.gradle")
        "message" | "distributionKey" | "distributionReleaseNote" | true         | true         | true         | new File("build.gradle")
    }

    @Unroll
    def "create a inputParams for apk file handling"() {
        setup:
        def deployment = project.objects.newInstance(DeploymentConfiguration)
        deployment.sourceFilePath.set(sourceFile?.absolutePath)

        and:
        def apkInfo = new DirectApkInfo("dep1", apkFile, false, false)

        and:
        def inputParams = UploadApkTask.createInputParams(apkInfo, deployment)

        expect:
        inputParams.artifactFilePath == (sourceFile ?: apkFile).absolutePath

        where:
        sourceFile               | apkFile
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build2.gradle")
    }

}
