package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.DirectAabInfo
import com.deploygate.gradle.plugins.dsl.Distribution
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import spock.lang.Specification
import spock.lang.Unroll

class UploadAabTaskInputParamsSpec extends Specification {

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
        def aabInfo = new DirectAabInfo("dep1", aabFile)

        and:
        def inputParams = UploadAabTask.createInputParams(deployment, aabInfo)

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
        def deployment = new NamedDeployment("dep1")
        deployment.sourceFile = sourceFile

        and:
        def aabInfo = new DirectAabInfo("dep1", aabFile)

        and:
        def inputParams = UploadAabTask.createInputParams(deployment, aabInfo)

        expect:
        inputParams.artifactFilePath == (sourceFile ?: aabFile).absolutePath

        where:
        sourceFile               | aabFile
        null                     | new File("build.gradle")
        new File("build.gradle") | null
        new File("build.gradle") | new File("build.gradle")
    }

}
