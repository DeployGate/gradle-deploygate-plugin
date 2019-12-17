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
        uploadParams.visibility = visibility

        and:
        def params = uploadParams.toMap()

        expect:
        params["message"] == message
        params["distribution_key"] == distributionKey
        params["release_note"] == releaseNote
        params["visibility"] == visibility
        message != null || !params.containsKey("message")
        distributionKey != null || !params.containsKey("distribution_key")
        releaseNote != null || !params.containsKey("release_note")
        visibility != null || !params.containsKey("visibility")

        where:
        message   | distributionKey   | releaseNote   | visibility | isSigningReady | isUniversalApk | apkFile
        null            | null              | null          | null       | false          | false          | null
        "message" | "distributionKey" | "releaseNote" | "public"   | true           | true           | new File("build.gradle")
    }
}
