package com.deploygate.gradle.plugins

import org.junit.rules.TemporaryFolder

class TestAndroidProject {
    private TemporaryFolder temporaryFolder

    TestAndroidProject(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder
    }

    void copyFromResources() {
        def classLoader = getClass().getClassLoader()
        def projectDir = classLoader.getResource("project").file as File

        projectDir.listFiles().each { File f ->
            copy(f, temporaryFolder.root)
        }

        def androidSdk = System.getenv("ANDROID_HOME") ?: System.getenv("HOME") + "/Library/Android/sdk"
        def localProperties = temporaryFolder.newFile("local.properties")
        localProperties << """
sdk.dir=${androidSdk}
ndk.dir=${androidSdk}/ndk-bundle
"""
    }

    private void copy(File copyFrom, File copyTo) {
        def nextCopyTo = new File(copyTo, copyFrom.name)

        if (copyFrom.isFile()) {
            nextCopyTo << copyFrom.newInputStream()
        } else if (copyFrom.isDirectory()) {
            if (!nextCopyTo.mkdirs()) {
                throw new RuntimeException("cannot make directories")
            }

            copyFrom.listFiles().each { File f ->
                copy(f, nextCopyTo)
            }
        }
    }

    File getBuildGradle() {
        return new File(temporaryFolder.root, "build.gradle")
    }

    void gradleProperties(Map<String, Object> vars) {
        temporaryFolder.newFile("gradle.properties") << vars.collect { "${it.key}=${it.value}" }.join("\n")
    }
}
