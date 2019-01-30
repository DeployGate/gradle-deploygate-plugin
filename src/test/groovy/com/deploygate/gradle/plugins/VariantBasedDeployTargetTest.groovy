package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import org.junit.Test

class VariantBasedDeployTargetTest {
    @Test
    public void apkTest() {
        String name            = "name"
        File file              = null
        String message         = "test message"
        String distributionKey = "test distribution key"
        String releaseNote     = "test release note"
        String visibility      = "public"
        boolean noAssemble     = true

        VariantBasedDeployTarget apk = new VariantBasedDeployTarget(name: name, sourceFile: file, message: message, distributionKey: distributionKey, releaseNote: releaseNote, visibility: visibility, noAssemble: noAssemble)
        checkDeployTarget(apk, name, file, message, distributionKey, releaseNote, visibility, noAssemble)
        checkParams(apk, message, distributionKey, releaseNote, visibility)
    }

    @Test
    public void argsNullTest() {
        String name            = "name"
        File file              = null
        String message         = null
        String distributionKey = null
        String releaseNote     = null
        String visibility      = null
        boolean noAssemble     = false

        VariantBasedDeployTarget apk = new VariantBasedDeployTarget(name)
        apk.sourceFile = file
        checkDeployTarget(apk, name, file, message, distributionKey, releaseNote, visibility, noAssemble)
        checkParams(apk, message, distributionKey, releaseNote, visibility)
    }

    public void checkDeployTarget(VariantBasedDeployTarget apk, String name, File file, String message, String distributionKey, String releaseNote, String visibility, boolean noAssemble) {
        assert apk instanceof VariantBasedDeployTarget
        assert apk.name == name
        assert apk.sourceFile == file
        assert apk.message == message
        assert apk.distributionKey == distributionKey
        assert apk.releaseNote == releaseNote
        assert apk.visibility == visibility
        assert apk.noAssemble == noAssemble
    }

    public void checkParams(VariantBasedDeployTarget apk, String message, String distributionKey, String releaseNote, String visibility) {
        HashMap<String, String> params = apk.toParams()
        assert params["message"] == message
        assert params["distribution_key"] == distributionKey
        assert params["release_note"] == releaseNote
        assert params["visibility"] == visibility
    }
}
