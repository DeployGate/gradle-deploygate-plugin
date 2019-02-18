package com.deploygate.gradle.plugins

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

class AcceptanceTestSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            options().port(8888)
    )

    @Nonnull
    TestAndroidProject testAndroidProject

    @Nonnull
    TestDeployGatePlugin testDeployGatePlugin

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)
        testDeployGatePlugin = new TestDeployGatePlugin()

        testAndroidProject.useAcceptanceResourceDir()

        wireMockRule.stubFor(
                post(urlPathEqualTo("/api/users/appOwner/apps")).willReturn(
                        ResponseDefinitionBuilder.okForJson([
                                "error"  : false,
                                "results": [
                                        "path"    : "",
                                        "revision": 2
                                ]
                        ])
                )
        )
    }

    @Unroll
    def "check tasks' existence #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)

        expect:
        runner.withArguments("existsTask", "-PtaskName=loginDeployGate").build()
        runner.withArguments("existsTask", "-PtaskName=logoutDeployGate").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGate").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor1Flavor3Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor2Flavor3Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor1Flavor4Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor2Flavor4Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor1Flavor3Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor2Flavor3Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor1Flavor4Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateFlavor2Flavor4Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateCustomApk").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAllParameter").build()

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "check task children of uploadDeployGate #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("showUploadDeployGateTaskNames")

        and:
        def result = runner.build().output

        expect: // declared tasks should be included
        !result.contains("loginDeployGate")
        !result.contains("logoutDeployGate")
        !result.contains("uploadDeployGateFlavor1Flavor3Release")
        !result.contains("uploadDeployGateFlavor2Flavor3Release")
        !result.contains("uploadDeployGateFlavor1Flavor4Release")
        !result.contains("uploadDeployGateFlavor2Flavor4Release")
        result.contains("uploadDeployGateFlavor1Flavor3Debug")
        result.contains("uploadDeployGateFlavor2Flavor3Debug")
        result.contains("uploadDeployGateFlavor1Flavor4Debug")
        result.contains("uploadDeployGateFlavor2Flavor4Debug")
        result.contains("uploadDeployGateCustomApk")
        result.contains("uploadDeployGateAllParameter")

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "flavor1Flavor3Debug #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("uploadDeployGateFlavor1Flavor3Debug" /*, "--stacktrace" */)

        and:
        runner.build()
        def loggedRequests = wireMockRule.findAll(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")))

        expect:
        loggedRequests.size() == 1
        loggedRequests[0].parts.find { it.name == "message" }.body.asString() == "flavor1Flavor3Debug"

//        wireMockRule.verify(1,
//                postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps"))
//                        .withRequestBodyPart(equalToInMessageBody("flavor1Flavor3Debug"))
//        )

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
//        "3.1.0"    | "4.4"
//        "3.2.0"    | "4.6"
//        "3.3.0"    | "4.10.1"
    }

    def "flavor2Flavor3Debug"() {

    }

    def "flavor1Flavor4Debug"() {

    }

    def "flavor2Flavor4Debug"() {

    }

    def "customApk"() {

    }

    def "allParameter"() {

    }

    private static MultipartValuePattern equalToInMessageBody(String message) {
        return new MultipartValuePatternBuilder("message")
                .withBody(equalTo(message))
                .build()
    }
}
