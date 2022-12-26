package com.deploygate.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

class AcceptanceKtsTestSpec extends AcceptanceTestBaseSpec {

    @Override
    void useProperResource() {
        testAndroidProject.useAcceptanceKtsResourceDir()
    }

    def "Backward compatibility #agpVersion"() {
        given:
        testAndroidProject.useGradleKtsForBackwardCompatibilityResource()
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)

        expect:
        runner.withArguments("existsTask", "-PtaskName=loginDeployGate").build()

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }
}
