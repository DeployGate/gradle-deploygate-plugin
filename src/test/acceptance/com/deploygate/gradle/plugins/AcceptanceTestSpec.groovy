package com.deploygate.gradle.plugins

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
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
        def buildResult = runner.build()
        def result = buildResult.output

        expect: // declared tasks should be included
        buildResult.task(":showUploadDeployGateTaskNames").outcome == TaskOutcome.SUCCESS
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
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateFlavor1Flavor3Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        missingPart(request, "message")
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "flavor2Flavor3Debug #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("uploadDeployGateFlavor2Flavor3Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateFlavor2Flavor3Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor2Flavor3Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "flavor1Flavor4Debug #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("uploadDeployGateFlavor1Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateFlavor1Flavor4Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor1Flavor4Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "flavor2Flavor4Debug should fail unless assembling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("uploadDeployGateFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.buildAndFail()

        expect:
        buildResult.task(":uploadDeployGateFlavor2Flavor4Debug").getOutcome() == TaskOutcome.FAILED

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "flavor2Flavor4Debug needs assembling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("assembleFlavor2Flavor4Debug", "uploadDeployGateFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":assembleFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        buildResult.task(":uploadDeployGateFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor2Flavor4Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    @Unroll
    def "customApk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(minGradleVersion)
                .withArguments("uploadDeployGateCustomApk" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateCustomApk").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "custom uploadMessage"
        request.getPart("distribution_key").body.asString() == "custom distributionKey"
        request.getPart("release_note").body.asString() == "custom releaseNote"
        request.getPart("visibility").body.asString() == "custom visibility"

        where:
        agpVersion | minGradleVersion
        "3.0.0"    | "4.1"
        "3.1.0"    | "4.4"
        "3.2.0"    | "4.6"
        "3.3.0"    | "4.10.1"
    }

    private static boolean missingPart(LoggedRequest request, String name) {
        return request.parts.isEmpty() || !request.parts.any { it.name == name }
    }
}
