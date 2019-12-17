package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectAabInfo
import com.deploygate.gradle.plugins.dsl.Distribution
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import spock.lang.Specification
import spock.lang.Unroll

class UploadAabTaskConfigurationSpec extends Specification {

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
        def aabInfo = new DirectAabInfo("dep1", null)

        and:
        def configuration = UploadAabTask.createConfiguration(deployment, aabInfo)

        expect:
        configuration.uploadParams.message == message
        configuration.uploadParams.distributionKey == distributionKey
        configuration.uploadParams.releaseNote == distributionReleaseNote
        configuration.uploadParams.visibility == visibility

        where:
        message   | distributionKey   | distributionReleaseNote   | visibility | skipAssemble
        null      | null              | null                      | null       | false
        "message" | "distributionKey" | "distributionReleaseNote" | "public"   | true
    }

    @Unroll
    def "create a configuration for aab file handling"() {
        setup:
        def deployment = new NamedDeployment("dep1")
        deployment.sourceFile = sourceFile

        and:
        def aabInfo = new DirectAabInfo("dep1", aabFile)

        and:
        def configuration = UploadAabTask.createConfiguration(deployment, aabInfo)

        expect:
        configuration.artifactFile == sourceFile ?: aabFile

        where:
        sourceFile               | aabFile
        null                     | null
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build.gradle")
    }

}
