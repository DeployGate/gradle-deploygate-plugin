package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.TestAndroidProject
import com.deploygate.gradle.plugins.TestDeployGatePlugin
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull

class GradleCompatAcceptanceSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    TestAndroidProject testAndroidProject

    @Nonnull
    TestDeployGatePlugin testDeployGatePlugin

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)
        testDeployGatePlugin = new TestDeployGatePlugin()

        testAndroidProject.useGradleCompatResource()
    }

    @Unroll
    def "check whether or not we can accept the build gradle. Unrolled #gradleVersion"() {
        given:
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("tasks" /*, "--stacktrace" */)

        and:
        def result = runner.build()

        expect:
        result.task("tasks").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << [
                "4.1",
                "4.4",
                "4.6",
                "4.8",
                "4.9",
                "4.10.1"
        ]
    }
}
