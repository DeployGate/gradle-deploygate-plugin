package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import spock.lang.Specification
import spock.lang.Unroll

class UploadApkTaskConfigurationSpec extends Specification {

    @Unroll
    def "create a configuration"() {
        setup:
        def deployment = new NamedDeployment("dep1")
        deployment.uploadMessage = uploadMessage
        deployment.distributionKey = distributionKey
        deployment.releaseNote = releaseNote
        deployment.visibility = visibility
        deployment.skipAssemble = skipAssemble

        and:
        def apkInfo = new DirectApkInfo("dep1", null, signingReady, universalApk)

        and:
        def configuration = UploadApkTask.createConfiguration(deployment, apkInfo)

        expect:
        configuration.uploadMessage == uploadMessage
        configuration.distributionKey == distributionKey
        configuration.releaseNote == releaseNote
        configuration.visibility == visibility
        configuration.isSigningReady == signingReady
        configuration.isUniversalApk == universalApk

        where:
        uploadMessage   | distributionKey   | releaseNote   | visibility | skipAssemble | signingReady | universalApk
        null            | null              | null          | null       | false        | false        | false
        "uploadMessage" | "distributionKey" | "releaseNote" | "public"   | true         | true         | true
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
        configuration.apkFile == sourceFile ?: apkFile

        where:
        sourceFile               | apkFile
        null                     | null
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build.gradle")
    }

    @Unroll
    def "toUploadParams should not contain null values"() {
        setup:
        def configuration = new UploadApkTask.Configuration()
        configuration.apkFile = apkFile
        configuration.uploadMessage = uploadMessage
        configuration.distributionKey = distributionKey
        configuration.releaseNote = releaseNote
        configuration.visibility = visibility
        configuration.isSigningReady = isSigningReady
        configuration.isUniversalApk = isUniversalApk

        and:
        def params = configuration.toUploadParams()

        expect:
        params["message"] == uploadMessage
        params["distribution_key"] == distributionKey
        params["release_note"] == releaseNote
        params["visibility"] == visibility
        uploadMessage != null || !params.containsKey("message")
        distributionKey != null || !params.containsKey("distribution_key")
        releaseNote != null || !params.containsKey("release_note")
        visibility != null || !params.containsKey("visibility")

        where:
        uploadMessage   | distributionKey   | releaseNote   | visibility | isSigningReady | isUniversalApk | apkFile
        null            | null              | null          | null       | false          | false          | null
        "uploadMessage" | "distributionKey" | "releaseNote" | "public"   | true           | true           | new File("build.gradle")
    }
}
