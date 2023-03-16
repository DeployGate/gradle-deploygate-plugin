package com.deploygate.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AcceptanceTestBaseSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    TestAndroidProject testAndroidProject

    @NotNull
    TestDeployGatePlugin testDeployGatePlugin

    abstract void useProperResource()

    def setup() {
        testAndroidProject = new TestAndroidProject(testProjectDir)
        testDeployGatePlugin = new TestDeployGatePlugin()

        useProperResource()
    }

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

        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Debug").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor3Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor3Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor1Flavor4Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabFlavor2Flavor4Release").build()
        runner.withArguments("existsTask", "-PtaskName=uploadDeployGateAabCustomApk").build()

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        buildResult.task(":uploadDeployGateFlavor1Flavor3Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateFlavor1Flavor3Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        buildResult.task(":uploadDeployGateAabFlavor1Flavor3Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateAabFlavor1Flavor3Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        buildResult.task(":uploadDeployGateFlavor2Flavor3Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateFlavor2Flavor3Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        buildResult.task(":uploadDeployGateAabFlavor2Flavor3Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateAabFlavor2Flavor3Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        buildResult.task(":uploadDeployGateFlavor1Flavor4Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateFlavor1Flavor4Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

    def "flavor1Flavor4Debug aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
            "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabFlavor1Flavor4Debug", "--stacktrace")

        and:
        def buildResult = runner.build()

        expect:
        buildResult.task(":uploadDeployGateAabFlavor1Flavor4Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateAabFlavor1Flavor4Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        assembleBuildResult.task(":assembleFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        uploadBuildResult.task(":uploadDeployGateFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateFlavor2Flavor4Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

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

        expect:
        assembleBuildResult.task(":bundleFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        uploadBuildResult.task(":uploadDeployGateAabFlavor2Flavor4Debug").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateAabFlavor2Flavor4Debug/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

    def "customApk apk #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
            "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateCustomApk", "--stacktrace")

        and:
        def buildResult = runner.build()

        expect:
        buildResult.task(":uploadDeployGateCustomApk").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateCustomApk/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }

    def "customApk aab #agpVersion"() {
        given:
        testAndroidProject.gradleProperties([
            "agpVersion": agpVersion
        ])

        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath(testDeployGatePlugin.loadPluginClasspath())
                .withGradleVersion(gradleVersion)
                .withArguments("uploadDeployGateAabCustomApk", "--stacktrace")

        and:
        def buildResult = runner.build()

        expect:
        buildResult.task(":uploadDeployGateAabCustomApk").outcome == TaskOutcome.SUCCESS
        new File(testProjectDir.root, "build/deploygate/uploadDeployGateAabCustomApk/response.json").size() > 0

        where:
        agpVersion = System.getenv("TEST_AGP_VERSION")
        gradleVersion = System.getenv("TEST_GRADLE_VERSION")
    }
}
