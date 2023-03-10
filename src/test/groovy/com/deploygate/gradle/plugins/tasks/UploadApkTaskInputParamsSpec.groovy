package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.dsl.Distribution
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import spock.lang.Specification
import spock.lang.Unroll

class UploadApkTaskInputParamsSpec extends Specification {

    @Unroll
    def "create a inputParams"() {
        setup:
        def deployment = new NamedDeployment("dep1")
        deployment.message = message
        deployment.distribution { Distribution distribution ->
            distribution.key = distributionKey
            distribution.releaseNote = distributionReleaseNote
        }
        deployment.skipAssemble = skipAssemble

        and:
        def apkInfo = new DirectApkInfo("dep1", apkFile, signingReady, universalApk)

        and:
        def inputParams = UploadApkTask.createInputParams(deployment, apkInfo)

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
        def deployment = new NamedDeployment("dep1")
        deployment.sourceFile = sourceFile

        and:
        def apkInfo = new DirectApkInfo("dep1", apkFile, false, false)

        and:
        def inputParams = UploadApkTask.createInputParams(deployment, apkInfo)

        expect:
        inputParams.artifactFilePath == (sourceFile ?: apkFile).absolutePath

        where:
        sourceFile               | apkFile
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build.gradle")
    }

}
