package com.deploygate.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

class AcceptanceKtsTestSpec extends AcceptanceTestBaseSpec {

    @Override
    void useProperResource() {
        testAndroidProject.useAcceptanceKtsResourceDir()
    }

    @Override
    AGPEnv[] getTestTargetAGPEnvs() {
        return [
                new AGPEnv("3.3.2", "4.10.1"),
                new AGPEnv("3.4.0", "5.1.1"),
                new AGPEnv("3.5.0", "5.4.1"),
        ]
    }

    @Unroll
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
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }
}
