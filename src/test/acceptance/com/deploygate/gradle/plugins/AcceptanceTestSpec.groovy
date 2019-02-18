package com.deploygate.gradle.plugins

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class AcceptanceTestSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    TestAndroidProject testAndroidProject

    @Nonnull
    List<File> pluginClasspath

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)

        testAndroidProject.copyFromResources()

        def pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")

        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }


}
