package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DeployGateExtensionSpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

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
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments, new CliCredentialStore(File.createTempDir())))
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
        dep1.distribution.key == "distributionKey1"
        dep1.distribution.releaseNote == "releaseNote1"

        when:
        def dep2 = extension.deployments.findByName("dep2")

        then:
        dep2.name == "dep2"
        !dep2.hasDistribution()
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
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments, new CliCredentialStore(File.createTempDir())))
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
        dep1.distribution.key == null
        dep1.distribution.releaseNote == null
        !dep1.skipAssemble

        when:
        def dep2 = result.deployments.findByName("dep2")

        then:
        dep2.sourceFile.name == "build.gradle"
        dep2.message == "message1"
        dep2.distribution.key == "distKey"
        dep2.distribution.releaseNote == "note1"
        dep2.skipAssemble
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
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments, new CliCredentialStore(File.createTempDir())))
        project.evaluate()

        when:
        def result = project.deploygate as DeployGateExtension

        then:
        result.hasDeployment("dep1")
        result.hasDeployment("dep2")
        !result.hasDeployment("dep3")
    }
}
