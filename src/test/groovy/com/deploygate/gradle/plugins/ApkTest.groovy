package com.deploygate.gradle.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

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

    @Test
    public void projectTest() {
        def project = new ProjectBuilder().withProjectDir(new File('src/test/project')).build()
        project.apply plugin: 'deploygate'
        project.deploygate {}
        project.evaluate()

        def apks = Apk.getApks(project)

        String testApkName = "Test"
        File testApkFile = null
        String testApkMessage = "test message"
        String testApkDistributionKey = "key"
        String testApkReleaseNote = "release note"
        String testApkVisibility = "private"
        Apk testApk = apks.get(0)
        checkApk(testApk, testApkName, testApkFile, testApkMessage, testApkDistributionKey, testApkReleaseNote, testApkVisibility)
        checkParams(testApk, testApkMessage, testApkDistributionKey, testApkReleaseNote, testApkVisibility)

        String test2ApkName = "Test2"
        File test2ApkFile = null
        String test2ApkMessage = ""
        String test2ApkDistributionKey = null
        String test2ApkReleaseNote = null
        String test2ApkVisibility = "public"
        Apk test2Apk = apks.get(1)
        checkApk(test2Apk, test2ApkName, test2ApkFile, test2ApkMessage, test2ApkDistributionKey, test2ApkReleaseNote, test2ApkVisibility)
        checkParams(test2Apk, test2ApkMessage, test2ApkDistributionKey, test2ApkReleaseNote, test2ApkVisibility)
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
