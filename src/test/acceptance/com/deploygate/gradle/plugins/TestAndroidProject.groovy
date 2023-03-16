package com.deploygate.gradle.plugins

import org.jetbrains.annotations.NotNull
import org.junit.rules.TemporaryFolder

class TestAndroidProject {
    private TemporaryFolder temporaryFolder

    TestAndroidProject(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder
    }

    void useProjectResourceDir() {
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

    void useAcceptanceResourceDir() {
        useProjectResourceDir()
        buildGradle.exists() && buildGradle.delete()
        copyDir("acceptance")
    }

    void useAcceptanceKtsResourceDir() {
        useAcceptanceResourceDir()
        useGradleKtsResource()
    }

    void useGradleKtsForBackwardCompatibilityResource() {
        def classLoader = getClass().getClassLoader()
        def dir = classLoader.getResource("acceptance-kts").file as File

        buildGradle.exists() && buildGradle.delete()
        buildGradleKts.exists() && buildGradleKts.delete() && buildGradleKts.createNewFile()
        buildGradleKts << new File(dir, "old-dsl.build.gradle.kts").newInputStream()
    }

    private void useGradleKtsResource() {
        def classLoader = getClass().getClassLoader()
        def dir = classLoader.getResource("acceptance-kts").file as File

        buildGradle.exists() && buildGradle.delete()
        buildGradleKts.exists() && buildGradleKts.delete() && buildGradleKts.createNewFile()
        buildGradleKts << new File(dir, "build.gradle.kts").newInputStream()
    }

    @NotNull
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
        nextCopyTo.exists() && nextCopyTo.delete()

        if (copyFrom.isFile()) {
            if (!nextCopyTo.createNewFile()) {
                throw new RuntimeException("cannot make a file")
            }
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

    File getBuildGradleKts() {
        return new File(temporaryFolder.root, "build.gradle.kts")
    }

    void gradleProperties(Map<String, Object> vars) {
        temporaryFolder.newFile("gradle.properties") << vars.collect { "${it.key}=${it.value}" }.join("\n")
    }
}
