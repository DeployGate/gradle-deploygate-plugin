package com.deploygate.gradle.plugins.internal.agp

import com.deploygate.gradle.plugins.TestAndroidProject
import com.deploygate.gradle.plugins.TestDeployGatePlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull

class AndroidGradlePluginAcceptanceSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    TestAndroidProject testAndroidProject

    @Nonnull
    TestDeployGatePlugin testDeployGatePlugin

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)
        testDeployGatePlugin = new TestDeployGatePlugin()

        testAndroidProject.useProjectResourceDir()
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
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("printAGPVersion" /*, "--stacktrace" */)

        and:
        def result = runner.build()

        expect:
        result.output.trim().contains(expectedAgpVersion)

        where:
        agpVersion       | minGradleVersion | expectedAgpVersion
        "3.5.0"          | "5.4.1"          | "3.5.0"
        "3.6.0"          | "5.6.4"          | "3.6.0"
        "4.0.0"          | "6.1.1"          | "4.0.0"
        "4.1.0"          | "6.5"            | "4.1.0"
        "4.2.0"          | "6.7.1"          | "4.2.0"
    }
}
