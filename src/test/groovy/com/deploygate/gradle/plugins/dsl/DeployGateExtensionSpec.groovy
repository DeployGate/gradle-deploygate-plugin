package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.TestSystemEnv
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class DeployGateExtensionSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Rule
    TestSystemEnv testSystemEnv

    File buildGradle

    def setup() {
        buildGradle = testProjectDir.newFile("build.gradle")
        testProjectDir.newFile("settings.gradle") << "rootProject.name = 'ok'"
    }

    def "backward check"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        def buildFileContent = """
deploygate {
  userName = "user1"
  token = "token1"
  apks {
    dep1 {
    }
  }
}
"""

        buildGradle.exists() && buildGradle.delete() && buildGradle.createNewFile()
        buildGradle << buildFileContent

        and:
        NamedDomainObjectContainer<VariantBasedDeployTarget> targets = project.container(VariantBasedDeployTarget)
        project.extensions.add("deploygate", new DeployGateExtension(project, targets))
        project.evaluate()

        when:
        def result = project.deploygate as DeployGateExtension

        then:
        result.appOwnerName == "user1"
        result.apiToken == "token1"
        result.deployments*.name.sort() == ["dep1"].sort()
    }

    def "can accept a given extension"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        def buildFileContent = """
deploygate {
  appOwnerName = "user1"
  apiToken = "token1"
  deployments {
    dep1 {
    }
    dep2 {
      sourceFile = "build.gradle" as File
      distributionKey = "distKey"
      releaseNote = "note1"
      uploadMessage = "message1"
      skipAssemble = true
    }
  }
}
"""

        buildGradle.exists() && buildGradle.delete() && buildGradle.createNewFile()
        buildGradle << buildFileContent

        and:
        NamedDomainObjectContainer<VariantBasedDeployTarget> targets = project.container(VariantBasedDeployTarget)
        project.extensions.add("deploygate", new DeployGateExtension(project, targets))
        project.evaluate()

        when:
        def result = project.deploygate as DeployGateExtension

        then:
        result.appOwnerName == "user1"
        result.apiToken == "token1"
        result.deployments*.name.sort() == ["dep1", "dep2"].sort()

        when:
        def dep1 = result.deployments.findByName("dep1")

        then:
        dep1.sourceFile == null
        dep1.uploadMessage == null
        dep1.releaseNote == null
        dep1.distributionKey == null
        !dep1.skipAssemble

        when:
        def dep2 = result.deployments.findByName("dep2")

        then:
        dep2.sourceFile.name == "build.gradle"
        dep2.uploadMessage == "message1"
        dep2.releaseNote == "note1"
        dep2.distributionKey == "distKey"
        dep2.skipAssemble
    }

    def "mergeDeployTarget should work"() {
        given:
        def base = new VariantBasedDeployTarget("base")
        base.sourceFile = new File("base")
        base.uploadMessage = "base"
        base.distributionKey = "base"
        base.releaseNote = "base"
        base.visibility = "base"
        base.skipAssemble = false

        and:
        def other = new VariantBasedDeployTarget("other")

        when:
        DeployGateExtension.mergeDeployTarget(base, other)

        then:
        base.sourceFile == new File("base")
        base.uploadMessage == "base"
        base.distributionKey == "base"
        base.releaseNote == "base"
        base.visibility == "base"
        !base.skipAssemble

        when:
        other.sourceFile = new File("other")
        other.uploadMessage = "other uploadMessage"
        other.distributionKey = "other distributionKey"
        other.releaseNote = "other releaseNote"
        other.visibility = "other visibility"
        other.skipAssemble = true

        and:
        DeployGateExtension.mergeDeployTarget(base, other)

        then:
        base.sourceFile == new File("base")
        base.uploadMessage == "base"
        base.distributionKey == "base"
        base.releaseNote == "base"
        base.visibility == "base"
        base.skipAssemble // only skip assemble was changed

        when:
        base.sourceFile = null
        base.uploadMessage = null
        base.distributionKey = null
        base.releaseNote = null
        base.visibility = null
        base.skipAssemble = false

        and:
        DeployGateExtension.mergeDeployTarget(base, other)

        then:
        base.sourceFile == new File("other")
        base.uploadMessage == "other uploadMessage"
        base.distributionKey == "other distributionKey"
        base.releaseNote == "other releaseNote"
        base.visibility == "other visibility"
        base.skipAssemble

        when:
        base.sourceFile = null
        base.uploadMessage = ""
        base.distributionKey = ""
        base.releaseNote = ""
        base.visibility = ""
        base.skipAssemble = false

        and:
        DeployGateExtension.mergeDeployTarget(base, other)

        then:
        base.sourceFile == new File("other")
        base.uploadMessage == "other uploadMessage"
        base.distributionKey == "other distributionKey"
        base.releaseNote == "other releaseNote"
        base.visibility == "other visibility"
        base.skipAssemble
    }

    @Unroll
    def "getDefaultDeployTarget should return based on env. Unrolled #sourceFilePath"() {
        given:
        def env = [:]
        env[DeployGatePlugin.ENV_NAME_SOURCE_FILE] = sourceFilePath
        env[DeployGatePlugin.ENV_NAME_UPLOAD_MESSAGE] = uploadMessage
        env[DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY] = distributionKey
        env[DeployGatePlugin.ENV_NAME_RELEASE_NOTE] = releaseNote
        env[DeployGatePlugin.ENV_NAME_VISIBILITY] = visibility
        testSystemEnv.setEnv(env)

        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        VariantBasedDeployTarget target = DeployGateExtension.getDefaultDeployTarget(project)

        expect:
        target.sourceFile == sourceFilePath?.with { project.file(sourceFilePath) }
        target.uploadMessage == uploadMessage
        target.distributionKey == distributionKey
        target.releaseNote == releaseNote
        target.visibility == visibility
        !target.skipAssemble // this var cannot be injected from env vars for now

        where:
        sourceFilePath   | uploadMessage   | distributionKey   | releaseNote   | visibility   | skipAssemble
        null             | null            | null              | null          | null         | null
        "sourceFilePath" | "uploadMessage" | "distributionKey" | "releaseNote" | "visibility" | true
    }
}
