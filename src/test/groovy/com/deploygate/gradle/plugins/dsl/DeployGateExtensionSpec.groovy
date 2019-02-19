package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.TestSystemEnv
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DeployGateExtensionSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Rule
    TestSystemEnv testSystemEnv

    File buildGradle

    def setup() {
        buildGradle = testProjectDir.newFile("build.gradle")
    }

    def "backward check"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        testProjectDir.newFile("settings.gradle") << "rootProject.name = 'ok'"

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
        testProjectDir.newFile("settings.gradle") << "rootProject.name = 'ok'"

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
    }
}
