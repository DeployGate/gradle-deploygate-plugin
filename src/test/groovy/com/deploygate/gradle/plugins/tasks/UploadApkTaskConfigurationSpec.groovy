package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.dsl.Distribution
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import spock.lang.Specification
import spock.lang.Unroll

class UploadApkTaskConfigurationSpec extends Specification {

    @Unroll
    def "create a configuration"() {
        setup:
        def deployment = new NamedDeployment("dep1")
        deployment.message = message
        deployment.distribution { Distribution distribution ->
            distribution.key = distributionKey
            distribution.releaseNote = distributionReleaseNote
        }
        deployment.visibility = visibility
        deployment.skipAssemble = skipAssemble

        and:
        def apkInfo = new DirectApkInfo("dep1", null, signingReady, universalApk)

        and:
        def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

        expect:
        configuration.uploadParams.message == message
        configuration.uploadParams.distributionKey == distributionKey
        configuration.uploadParams.releaseNote == distributionReleaseNote
        configuration.uploadParams.visibility == visibility
        configuration.isSigningReady == signingReady
        configuration.isUniversalApk == universalApk

        where:
        message   | distributionKey   | distributionReleaseNote   | visibility | skipAssemble | signingReady | universalApk
        null            | null              | null                      | null       | false        | false        | false
        "message" | "distributionKey" | "distributionReleaseNote" | "public"   | true         | true         | true
    }

    @Unroll
    def "create a configuration for apk file handling"() {
        setup:
        def deployment = new NamedDeployment("dep1")
        deployment.sourceFile = sourceFile

        and:
        def apkInfo = new DirectApkInfo("dep1", apkFile, false, false)

        and:
        def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

        expect:
        configuration.artifactFile == sourceFile ?: apkFile

        where:
        sourceFile               | apkFile
        null                     | null
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build.gradle")
    }

}
