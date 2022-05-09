package com.deploygate.gradle.plugins.tasks

import spock.lang.Specification
import spock.lang.Unroll

class UploadArtifactTaskUploadParamsSpec extends Specification {

    @Unroll
    def "toMap should not contain null values"() {
        setup:
        def uploadParams = new UploadArtifactTask.UploadParams()
        uploadParams.message = message
        uploadParams.distributionKey = distributionKey
        uploadParams.releaseNote = releaseNote

        and:
        def params = uploadParams.toMap()

        expect:
        params["message"] == message
        params["distribution_key"] == distributionKey
        params["release_note"] == releaseNote
        message != null || !params.containsKey("message")
        distributionKey != null || !params.containsKey("distribution_key")
        releaseNote != null || !params.containsKey("release_note")

        where:
        message   | distributionKey   | releaseNote   | isSigningReady | isUniversalApk | apkFile
        null            | null              | null          | false          | false          | null
        "message" | "distributionKey" | "releaseNote" | true           | true           | new File("build.gradle")
    }
}
