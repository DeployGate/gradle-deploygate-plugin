package com.deploygate.gradle.plugins.dsl


import spock.lang.Specification
import spock.lang.Unroll

class NamedDeploymentSpec extends Specification {

    @Unroll
    def "verify deprecated fields of NamedDeployment. Unrolled #name"() {
        setup:
        def deployment = new NamedDeployment(name)
        deployment.message = uploadMessage
        deployment.noAssemble = skipAssemble

        expect:
        deployment.name == name
        deployment.uploadMessage == uploadMessage
        deployment.skipAssemble == skipAssemble

        where:
        name  | uploadMessage | skipAssemble
        "foo" | null          | true
        "bar" | "message"     | false
    }

    @Unroll
    def "verify NamedDeployment works. Unrolled #name"() {
        setup:
        def deployment = new NamedDeployment(name)
        deployment.uploadMessage = uploadMessage
        deployment.skipAssemble = skipAssemble
        deployment.sourceFile = sourceFile
        deployment.distributionKey = distributionKey
        deployment.releaseNote = releaseNote
        deployment.visibility = visibility

        expect:
        deployment.name == name
        deployment.uploadMessage == uploadMessage
        deployment.skipAssemble == skipAssemble
        deployment.sourceFile == sourceFile
        deployment.distributionKey == distributionKey
        deployment.releaseNote == releaseNote
        deployment.visibility == visibility

        where:
        name  | uploadMessage | skipAssemble | sourceFile      | distributionKey    | releaseNote    | visibility
        "foo" | null          | true         | null            | null               | null           | null
        "bar" | "message"     | false        | new File("apk") | "distribution_key" | "release_note" | "private"
        "bar" | "message"     | false        | new File("apk") | "distribution_key" | "release_note" | "public"
    }

    def "verify empty NamedDeployment works"() {
        setup:
        def deployment = new NamedDeployment("name")

        expect:
        deployment.name == "name"
        deployment.uploadMessage == null
        !deployment.skipAssemble
        deployment.sourceFile == null
        deployment.distributionKey == null
        deployment.releaseNote == null
        deployment.visibility == null
    }
}
