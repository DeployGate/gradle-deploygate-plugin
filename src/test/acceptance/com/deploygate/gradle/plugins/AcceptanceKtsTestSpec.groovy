package com.deploygate.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import spock.lang.IgnoreIf
import spock.lang.Unroll

@IgnoreIf({ Boolean.valueOf(env["NO_KTS_SUPPORT"]) })
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
