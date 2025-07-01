package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class UploadApkTaskInputParamsSpec extends Specification {

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
        def apkInfo = new DirectApkInfo("dep1", apkFile, signingReady, universalApk)

        and:
        def artifactFileProvider = project.providers.provider {
            def f = new File(deployment.sourceFilePath.getOrElse(apkInfo.apkFile?.absolutePath))
            f.exists() ? f : null
        }
        def inputParams = UploadApkTask.createInputParams(apkInfo, deployment, artifactFileProvider)

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
        def artifactFileProvider = project.providers.provider {
            def f = new File(deployment.sourceFilePath.getOrElse(apkInfo.apkFile?.absolutePath))
            f.exists() ? f : null
        }
        def inputParams = UploadApkTask.createInputParams(apkInfo, deployment, artifactFileProvider)

        expect:
        inputParams.artifactFilePath == (sourceFile ?: apkFile).absolutePath

        where:
        sourceFile               | apkFile
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build2.gradle")
    }
}
