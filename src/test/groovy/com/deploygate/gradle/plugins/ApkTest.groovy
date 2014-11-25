package com.deploygate.gradle.plugins

import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Created by kenta.imai on 2014/11/25.
 */
class ApkTest {
    @Test
    public void apkTest() {
        String name            = "name"
        File file              = null
        String message         = "test message"
        String distributionKey = "test distribution key"
        String releaseNote     = "test release note"
        String visibility      = "public"

        Apk apk = new Apk(name, file, message, distributionKey, releaseNote, visibility)
        checkApk(apk, name, file, message, distributionKey, releaseNote, visibility)
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

        Apk apk = new Apk(name, file)
        checkApk(apk, name, file, message, distributionKey, releaseNote, visibility)
        checkParams(apk, message, distributionKey, releaseNote, visibility)
    }

    public void checkApk(Apk apk, String name, File file, String message, String distributionKey, String releaseNote, String visibility) {
        assertTrue(apk instanceof Apk)
        assertTrue(apk.name == name)
        assertTrue(apk.file == file)
        assertTrue(apk.message == message)
        assertTrue(apk.distributionKey == distributionKey)
        assertTrue(apk.releaseNote == releaseNote)
        assertTrue(apk.visibility == visibility)
    }

    public void checkParams(Apk apk, String message, String distributionKey, String releaseNote, String visibility) {
        HashMap<String, String> params = apk.getParams()
        assertTrue(params["message"] == message)
        assertTrue(params["distribution_key"] == distributionKey)
        assertTrue(params["release_note"] == releaseNote)
        assertTrue(params["visibility"] == visibility)
    }
}
