package com.deploygate.gradle.plugins.dsl

import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

class VariantBasedDeployTargetImplSpec extends Specification {
    private static Object SKIP = new Object()

    @Unroll
    def "verify exposed fields of VariantBasedDeployTarget. Unrolled #baz"() {
        expect:
        def deployTarget = new VariantBasedDeployTargetImpl(

        )

        where:
        name  | sourceFile               | uploadMessage | distributionKey     | releaseNote     | visibility | noAssemble
        "foo" | null                     | null          | null                | null            | null       | true
        "bar" | new File("a")            | "message"     | "distribution_key1" | "release_note1" | "private"  | false
        "baz" | new File("build.gradle") | SKIP          | SKIP                | SKIP            | SKIP       | SKIP
    }

    @Test
    public void variantBasedDeployTargetTest() {
        String name = "name"
        File file = null
        String message = "test message"
        String distributionKey = "test distribution key"
        String releaseNote = "test release note"
        String visibility = "public"
        boolean noAssemble = true

        VariantBasedDeployTargetImpl deployTarget = new VariantBasedDeployTargetImpl(name: name, sourceFile: file, message: message, distributionKey: distributionKey, releaseNote: releaseNote, visibility: visibility, noAssemble: noAssemble)
        checkDeployTarget(deployTarget, name, file, message, distributionKey, releaseNote, visibility, noAssemble)
    }

    @Test
    public void argsNullTest() {
        String name = "name"
        File file = null
        String message = null
        String distributionKey = null
        String releaseNote = null
        String visibility = null
        boolean noAssemble = false

        VariantBasedDeployTargetImpl deployTarget = new VariantBasedDeployTargetImpl(name)
        deployTarget.sourceFile = file
        checkDeployTarget(deployTarget, name, file, message, distributionKey, releaseNote, visibility, noAssemble)
    }

    public void checkDeployTarget(VariantBasedDeployTargetImpl apk, String name, File file, String message, String distributionKey, String releaseNote, String visibility, boolean noAssemble) {
        assert apk instanceof VariantBasedDeployTargetImpl
        assert apk.name == name
        assert apk.sourceFile == file
        assert apk.message == message
        assert apk.distributionKey == distributionKey
        assert apk.releaseNote == releaseNote
        assert apk.visibility == visibility
        assert apk.noAssemble == noAssemble
    }

    public void checkGetDefaultDeployTarget() {
        // FIXME
    }
}
