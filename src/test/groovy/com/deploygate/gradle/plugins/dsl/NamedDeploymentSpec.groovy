package com.deploygate.gradle.plugins.dsl


import spock.lang.Specification
import spock.lang.Unroll

class NamedDeploymentSpec extends Specification {

    @Unroll
    def "verify deprecated fields of NamedDeployment. Unrolled #name"() {
        setup:
        def deployment = new NamedDeployment(name)
        deployment.message = message
        deployment.noAssemble = skipAssemble

        expect:
        deployment.name == name
        deployment.message == message
        deployment.skipAssemble == skipAssemble

        where:
        name  | message | skipAssemble
        "foo" | null          | true
        "bar" | "message"     | false
    }

    @Unroll
    def "verify NamedDeployment works. Unrolled #name"() {
        setup:
        def deployment = new NamedDeployment(name)
        deployment.message = message
        deployment.skipAssemble = skipAssemble
        deployment.sourceFile = sourceFile
        deployment.distribution { Distribution distribution ->
            distribution.key = distributionKey
            distribution.releaseNote = distributionReleaseNote
        }

        expect:
        deployment.name == name
        deployment.message == message
        deployment.skipAssemble == skipAssemble
        deployment.sourceFile == sourceFile
        deployment.distribution?.key == distributionKey
        deployment.distribution?.releaseNote == distributionReleaseNote

        where:
        name  | message | skipAssemble | sourceFile      | distributionKey    | distributionReleaseNote
        "foo" | null          | true         | null            | null               | null
        "bar" | "message"     | false        | new File("apk") | "distribution_key" | "distribution_release_note"
    }

    def "verify empty NamedDeployment works"() {
        setup:
        def deployment = new NamedDeployment("name")

        expect:
        deployment.name == "name"
        deployment.message == null
        !deployment.skipAssemble
        deployment.sourceFile == null
        deployment.distribution?.key == null
        deployment.distribution?.releaseNote == null
    }
}
