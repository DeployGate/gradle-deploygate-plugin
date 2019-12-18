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

abstract class AcceptanceTestBaseSpec extends Specification {

    static class AGPEnv {
        String agpVersion
        String gradleVersion

        AGPEnv(String agpVersion, String gradleVersion) {
            this.agpVersion = agpVersion
            this.gradleVersion = gradleVersion
        }
    }

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

    abstract void useProperResource()

    abstract AGPEnv[] getTestTargetAGPEnvs()

    AGPEnv[] getAppBundleTestTargetAGPEnvs() {
        return testTargetAGPEnvs.findAll { isAppBundleSupport(it.agpVersion) }
    }

    boolean isAppBundleSupport(String agpVersion) {
        def version = VersionString.tryParse(agpVersion)
        return version.major >= 4 || version.major == 3 && version.minor > 1
    }

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)
        testDeployGatePlugin = new TestDeployGatePlugin()

        useProperResource()

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
                .withGradleVersion(gradleVersion)

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

        if (isAppBundleSupport(agpVersion)) {
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Debug").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Debug").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Debug").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Debug").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Release").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Release").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Release").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Release").build()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabCustomApk").build()
        } else {
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Debug").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Debug").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Debug").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Debug").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Release").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Release").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Release").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Release").buildAndFail()
            runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabCustomApk").buildAndFail()
        }

        where:
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
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
                .withGradleVersion(gradleVersion)
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
        // regardless of aab support
        !result.contains("uploadDeployGateAabFlavor1Flavor3Release")
        !result.contains("uploadDeployGateAabFlavor2Flavor3Release")
        !result.contains("uploadDeployGateAabFlavor1Flavor4Release")
        !result.contains("uploadDeployGateAabFlavor2Flavor4Release")
        !result.contains("uploadDeployGateAabCustomApk")

        where:
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor1Flavor3Debug apk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
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
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor1Flavor3Debug aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor1Flavor3Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateAabFlavor1Flavor3Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        missingPart(request, "message")
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor3Debug apk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
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
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor3Debug aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor2Flavor3Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateAabFlavor2Flavor3Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor2Flavor3Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor1Flavor4Debug apk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
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
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor1Flavor4Debug aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor1Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateAabFlavor1Flavor4Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor1Flavor4Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor4Debug apk should fail unless assembling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.buildAndFail()

        expect:
        buildResult.task(":uploadDeployGateFlavor2Flavor4Debug").getOutcome() == TaskOutcome.FAILED

        where:
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor4Debug aab should fail unless bundling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def buildResult = runner.buildAndFail()

        expect:
        buildResult.task(":uploadDeployGateAabFlavor2Flavor4Debug").getOutcome() == TaskOutcome.FAILED

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor4Debug apk require assembling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def assembleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("assembleFlavor2Flavor4Debug" /*, "--stacktrace" */)

        def uploadRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def assembleBuildResult = assembleRunner.build()

        and:
        def uploadBuildResult = uploadRunner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        assembleBuildResult.task(":assembleFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        uploadBuildResult.task(":uploadDeployGateFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor2Flavor4Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "flavor2Flavor4Debug aab require bundling #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def assembleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("bundleFlavor2Flavor4Debug" /*, "--stacktrace" */)

        def uploadRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor2Flavor4Debug" /*, "--stacktrace" */)

        and:
        def assembleBuildResult = assembleRunner.build()

        and:
        def uploadBuildResult = uploadRunner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        assembleBuildResult.task(":bundleFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        uploadBuildResult.task(":uploadDeployGateAabFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "flavor2Flavor4Debug"
        missingPart(request, "distribution_key")
        missingPart(request, "release_note")
        missingPart(request, "visibility")

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "customApk apk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
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
        request.getPart("message").body.asString() == "custom message"
        request.getPart("distribution_key").body.asString() == "custom distributionKey"
        request.getPart("release_note").body.asString() == "custom releaseNote"
        request.getPart("visibility").body.asString() == "custom visibility"

        where:
        agpEnv << testTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    @Unroll
    def "customApk aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
                "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabCustomApk" /*, "--stacktrace" */)

        and:
        def buildResult = runner.build()
        def result = wireMockRule.findRequestsMatching(postRequestedFor(urlPathEqualTo("/api/users/appOwner/apps")).build())
        def request = result.requests.first()

        expect:
        buildResult.task(":uploadDeployGateAabCustomApk").outcome == TaskOutcome.SUCCESS
        result.requests.size() == 1
        request.getPart("token").body.asString() == "api token"
        request.getPart("file").body.present
        request.getPart("message").body.asString() == "custom message"
        request.getPart("distribution_key").body.asString() == "custom distributionKey"
        request.getPart("release_note").body.asString() == "custom releaseNote"
        request.getPart("visibility").body.asString() == "custom visibility"

        where:
        agpEnv << appBundleTestTargetAGPEnvs
        agpVersion = agpEnv.agpVersion as String
        gradleVersion = agpEnv.gradleVersion as String
    }

    private static boolean missingPart(LoggedRequest request, String name) {
        return request.parts.isEmpty() || !request.parts.any { it.name == name }
    }
}
