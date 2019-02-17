package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.TestAndroidProject
import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull

class AndroidGradlePluginSpec extends Specification {
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

    def "can apply this plugin to a project which does not have AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        "no-op"

        then:
        !AndroidGradlePlugin.isApplied(project)
    }

    def "can apply this plugin to a project which has AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'com.android.application'

        then:
        AndroidGradlePlugin.isApplied(project)
    }

    @Unroll
    def "feature catalog verification. Unrolled #agpVersion"() {
        given:
        AndroidGradlePlugin.metaClass.static.getVersion = { ->
            VersionString.tryParse(agpVersion)
        }

        expect:
        AndroidGradlePlugin.isAppBundleSupported() == isAppBundleSupported
        AndroidGradlePlugin.isSigningConfigCollectionSupported() == isSigningConfigCollectionSupported
        AndroidGradlePlugin.isTaskProviderBased() == isTaskProviderBased

        where:
        agpVersion                 | isAppBundleSupported | isSigningConfigCollectionSupported | isTaskProviderBased
        "3.0.0"                    | false                | false                              | false
        "3.1.0"                    | false                | false                              | false
        "3.2.0"                    | true                 | false                              | false
        "3.2.1"                    | true                 | false                              | false
        "3.3.0"                    | true                 | true                               | true
        "3.4.0"                    | true                 | true                               | true
        "4.0.0"                    | true                 | true                               | true // for now
        "${Integer.MAX_VALUE}.0.0" | true                 | true                               | true // fallback
    }

    /**
     * AGP 3.0.0 may cause an error *No toolchains found in the NDK toolchains folder for ABI with prefix: mips64el-linux-android*,
     * so please follow the steps below to solve it.
     *
     * cd  $ANDROID_HOME/ndk-bundle/toolchains
     * ln -s aarch64-linux-android-4.9 mips64el-linux-android
     * ln -s arm-linux-androideabi-4.9 mipsel-linux-android
     *
     * @return
     */
    @Unroll
    def "version verification. Unrolled #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(pluginClasspath)
                .withGradleVersion(minGradleVersion)
                .withArguments("printAGPVersion" /*, "--stacktrace" */)

        and:
        def result = runner.build()

        expect:
        result.output.trim().contains(agpVersion)

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }
}
