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
        buildGradle << """
        deploygate {
          userName = "user1"
          token = "token1"
          apks {
            dep1 {
              distributionKey = "distributionKey1"
              releaseNote = "releaseNote1"
            }
            dep2 {
            }
          }
        }
        """

        and:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))
        project.evaluate()

        when:
        def extension = project.deploygate as DeployGateExtension

        then:
        extension.appOwnerName == "user1"
        extension.apiToken == "token1"

        when:
        def dep1 = extension.deployments.findByName("dep1")

        then:
        dep1.name == "dep1"
        dep1.distribution
        dep1.distribution?.key == "distributionKey1"
        dep1.distribution?.releaseNote == "releaseNote1"

        when:
        def dep2 = extension.deployments.findByName("dep2")

        then:
        dep2.name == "dep2"
        !dep2.distribution
    }

    def "can accept a given extension"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        buildGradle << """
        deploygate {
          appOwnerName = "user1"
          apiToken = "token1"
          deployments {
            dep1 {
            }
            dep2 {
              sourceFile = "build.gradle" as File
              message = "message1"
              skipAssemble = true
              
              distribution {
                key = "distKey"
                releaseNote = "note1"
              }
            }
          }
        }
        """

        and:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))
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
        dep1.message == null
        dep1.distribution?.key == null
        dep1.distribution?.releaseNote == null
        !dep1.skipAssemble

        when:
        def dep2 = result.deployments.findByName("dep2")

        then:
        dep2.sourceFile.name == "build.gradle"
        dep2.message == "message1"
        dep2.distribution?.key == "distKey"
        dep2.distribution?.releaseNote == "note1"
        dep2.skipAssemble
    }

    def "findDeploymentByName"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        buildGradle << """
        deploygate {
          appOwnerName = "user1"
          apiToken = "token1"
          deployments {
            dep1 {
            }
            dep2 {
              sourceFile = "build.gradle" as File
              message = "message1"
              skipAssemble = true
              
              distribution {
                key = "distKey"
                releaseNote = "note1"
              }
            }
          }
        }
        """

        and:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))
        project.evaluate()

        when:
        def result = project.deploygate as DeployGateExtension

        then:
        result.findDeploymentByName("dep1") == result.deployments.findByName("dep1")
        result.findDeploymentByName("dep2") == result.deployments.findByName("dep2")

        when:
        def dep3 = result.findDeploymentByName("dep3")

        then:
        dep3
        dep3.name == "dep3"
        dep3.sourceFile == null
        dep3.message == null
        dep3.distribution?.key == null
        dep3.distribution?.releaseNote == null
        !dep3.skipAssemble
    }

    def "hasDeployment"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        buildGradle << """
        deploygate {
          appOwnerName = "user1"
          apiToken = "token1"
          deployments {
            dep1 {
            }
            dep2 {
              sourceFile = "build.gradle" as File
              message = "message1"
              skipAssemble = true
              
              distribution {
                key = "distKey"
                releaseNote = "note1"
              }
            }
          }
        }
        """

        and:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))
        project.evaluate()

        when:
        def result = project.deploygate as DeployGateExtension

        then:
        result.hasDeployment("dep1")
        result.hasDeployment("dep2")
        !result.hasDeployment("dep3")
    }

    def "mergeDeployments should work"() {
        given:
        def base = new NamedDeployment("base")
        base.sourceFile = new File("base")
        base.message = "base"
        base.distribution { Distribution distribution ->
            distribution.key = "base"
            distribution.releaseNote = "base"
        }
        base.skipAssemble = false

        and:
        def other = new NamedDeployment("other")

        when:
        DeployGateExtension.mergeDeployments(base, other)

        then:
        base.sourceFile == new File("base")
        base.message == "base"
        base.distribution?.key == "base"
        base.distribution?.releaseNote == "base"
        !base.skipAssemble

        when:
        other.sourceFile = new File("other")
        other.message = "other message"
        other.distribution { Distribution distribution ->
            distribution.key = "other distributionKey"
            distribution.releaseNote = "other distributionReleaseNote"
        }
        other.skipAssemble = true

        and:
        DeployGateExtension.mergeDeployments(base, other)

        then:
        base.sourceFile == new File("base")
        base.message == "base"
        base.distribution?.key == "base"
        base.distribution?.releaseNote == "base"
        base.skipAssemble // only skip assemble was changed

        when:
        base.sourceFile = null
        base.message = null
        base.distribution { Distribution distribution ->
            distribution.key = null
            distribution.releaseNote = null
        }
        base.skipAssemble = false

        and:
        DeployGateExtension.mergeDeployments(base, other)

        then:
        base.sourceFile == new File("other")
        base.message == "other message"
        base.distribution?.key == "other distributionKey"
        base.distribution?.releaseNote == "other distributionReleaseNote"
        base.skipAssemble

        when:
        base.sourceFile = null
        base.message = ""
        base.distribution { Distribution distribution ->
            distribution.key = ""
            distribution.releaseNote = ""
        }
        base.skipAssemble = false

        and:
        DeployGateExtension.mergeDeployments(base, other)

        then:
        base.sourceFile == new File("other")
        base.message == "other message"
        base.distribution?.key == "other distributionKey"
        base.distribution?.releaseNote == "other distributionReleaseNote"
        base.skipAssemble
    }

    @Unroll
    def "getEnvironmentBasedDeployments backward compatibility"() {
        given:
        def env = [:]
        env[DeployGatePlugin.ENV_NAME_DISTRIBUTION_RELEASE_NOTE] = distributionReleaseNote
        env[DeployGatePlugin.ENV_NAME_DISTRIBUTION_RELEASE_NOTE_V1] = v1DistributionReleaseNote
        testSystemEnv.setEnv(env)

        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        NamedDeployment deployment = DeployGateExtension.getEnvironmentBasedDeployment(project)

        expect:
        deployment.distribution?.releaseNote == expectedDistributionReleaseNote

        where:
        v1DistributionReleaseNote   | distributionReleaseNote   | expectedDistributionReleaseNote
        null                        | null                      | null
        null                        | "distributionReleaseNote" | "distributionReleaseNote"
        "v1DistributionReleaseNote" | null                      | "v1DistributionReleaseNote"
        "v1DistributionReleaseNote" | "distributionReleaseNote" | "distributionReleaseNote"
    }

    @Unroll
    def "getEnvironmentBasedDeployments should return based on env. Unrolled #sourceFilePath"() {
        given:
        def env = [:]
        env[DeployGatePlugin.ENV_NAME_SOURCE_FILE] = sourceFilePath
        env[DeployGatePlugin.ENV_NAME_MESSAGE] = message
        env[DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY] = distributionKey
        env[DeployGatePlugin.ENV_NAME_DISTRIBUTION_RELEASE_NOTE] = distributionReleaseNote
        testSystemEnv.setEnv(env)

        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        and:
        NamedDeployment deployment = DeployGateExtension.getEnvironmentBasedDeployment(project)

        expect:
        deployment.sourceFile == sourceFilePath?.with { project.file(sourceFilePath) }
        deployment.message == message
        deployment.distribution?.key == distributionKey
        deployment.distribution?.releaseNote == distributionReleaseNote
        !deployment.skipAssemble // this var cannot be injected from env vars for now

        where:
        sourceFilePath   | message   | distributionKey   | distributionReleaseNote   | skipAssemble
        null             | null      | null              | null                      | null
        "sourceFilePath" | "message" | "distributionKey" | "distributionReleaseNote" | true
    }
}
