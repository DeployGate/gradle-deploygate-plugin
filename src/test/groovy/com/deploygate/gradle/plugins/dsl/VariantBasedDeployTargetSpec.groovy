package com.deploygate.gradle.plugins.dsl


import spock.lang.Specification
import spock.lang.Unroll

class VariantBasedDeployTargetSpec extends Specification {

    @Unroll
    def "verify deprecated fields of VariantBasedDeployTarget. Unrolled #name"() {
        setup:
        def deployTarget = new NamedDeployment(name)
        deployTarget.message = uploadMessage
        deployTarget.noAssemble = skipAssemble

        expect:
        deployTarget.name == name
        deployTarget.uploadMessage == uploadMessage
        deployTarget.skipAssemble == skipAssemble

        where:
        name  | uploadMessage | skipAssemble
        "foo" | null          | true
        "bar" | "message"     | false
    }

    @Unroll
    def "verify VariantBasedDeployTarget works. Unrolled #name"() {
        setup:
        def deployTarget = new NamedDeployment(name)
        deployTarget.uploadMessage = uploadMessage
        deployTarget.skipAssemble = skipAssemble
        deployTarget.sourceFile = sourceFile
        deployTarget.distributionKey = distributionKey
        deployTarget.releaseNote = releaseNote
        deployTarget.visibility = visibility

        expect:
        deployTarget.name == name
        deployTarget.uploadMessage == uploadMessage
        deployTarget.skipAssemble == skipAssemble
        deployTarget.sourceFile == sourceFile
        deployTarget.distributionKey == distributionKey
        deployTarget.releaseNote == releaseNote
        deployTarget.visibility == visibility

        where:
        name  | uploadMessage | skipAssemble | sourceFile      | distributionKey    | releaseNote    | visibility
        "foo" | null          | true         | null            | null               | null           | null
        "bar" | "message"     | false        | new File("apk") | "distribution_key" | "release_note" | "private"
        "bar" | "message"     | false        | new File("apk") | "distribution_key" | "release_note" | "public"
    }

    def "verify empty VariantBasedDeployTarget works"() {
        setup:
        def deployTarget = new NamedDeployment("name")

        expect:
        deployTarget.name == "name"
        deployTarget.uploadMessage == null
        !deployTarget.skipAssemble
        deployTarget.sourceFile == null
        deployTarget.distributionKey == null
        deployTarget.releaseNote == null
        deployTarget.visibility == null
    }
}
