package com.deploygate.gradle.plugins

import org.junit.rules.TemporaryFolder

import javax.annotation.Nonnull

class TestAndroidProject {
    private TemporaryFolder temporaryFolder

    TestAndroidProject(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder
    }

    void copyFromResources() {
        def projectDir = copyDir("project")
        def localProperties = new File(projectDir, "local.properties")

        if (!localProperties.exists()) {
            localProperties.createNewFile()

            def androidSdk = System.getenv("ANDROID_HOME") ?: (System.getenv("HOME") + "/Library/Android/sdk")

            localProperties << """
sdk.dir=${androidSdk}
ndk.dir=${androidSdk}/ndk-bundle
"""
        }
    }

    void setupAcceptance() {
        copyFromResources()
        copyDir("acceptance")
    }

    @Nonnull
    private File copyDir(String dirName) {
        def classLoader = getClass().getClassLoader()
        def dir = classLoader.getResource(dirName).file as File

        dir.listFiles().each { File f ->
            copy(f, temporaryFolder.root)
        }

        return dir
    }

    private void copy(File copyFrom, File copyTo) {
        def nextCopyTo = new File(copyTo, copyFrom.name)

        if (copyFrom.isFile()) {
            nextCopyTo.exists() && nextCopyTo.delete() && nextCopyTo.createNewFile()
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
