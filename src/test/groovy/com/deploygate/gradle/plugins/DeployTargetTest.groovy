package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.entities.DeployTarget
import org.junit.Test

import static org.junit.Assert.assertTrue

class DeployTargetTest {
    @Test
    public void apkTest() {
        String name            = "name"
        File file              = null
        String message         = "test message"
        String distributionKey = "test distribution key"
        String releaseNote     = "test release note"
        String visibility      = "public"

        DeployTarget apk = new DeployTarget(name: name, sourceFile: file, message: message, distributionKey: distributionKey, releaseNote: releaseNote, visibility: visibility)
        checkDeployTarget(apk, name, file, message, distributionKey, releaseNote, visibility)
        checkParams(apk, message, distributionKey, releaseNote, visibility)
    }

    @Test
    public void argsNullTest() {
        String name            = "name"
        File file              = null
        String message         = ""
        String distributionKey = null
        String releaseNote     = null
        String visibility      = "private"

        DeployTarget apk = new DeployTarget(name)
        apk.sourceFile = file
        checkDeployTarget(apk, name, file, message, distributionKey, releaseNote, visibility)
        checkParams(apk, message, distributionKey, releaseNote, visibility)
    }

    public void checkDeployTarget(DeployTarget apk, String name, File file, String message, String distributionKey, String releaseNote, String visibility) {
        assertTrue(apk instanceof DeployTarget)
        assertTrue(apk.name == name)
        assertTrue(apk.sourceFile == file)
        assertTrue(apk.message == message)
        assertTrue(apk.distributionKey == distributionKey)
        assertTrue(apk.releaseNote == releaseNote)
        assertTrue(apk.visibility == visibility)
    }

    public void checkParams(DeployTarget apk, String message, String distributionKey, String releaseNote, String visibility) {
        HashMap<String, String> params = apk.toParams()
        assertTrue(params["message"] == message)
        assertTrue(params["distribution_key"] == distributionKey)
        assertTrue(params["release_note"] == releaseNote)
        assertTrue(params["visibility"] == visibility)
    }
}
