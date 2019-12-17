package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class DSLBasedUploadApkTaskFactorySpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    @Nonnull
    private DSLBasedUploadApkTaskFactory dslBasedUploadApkTaskFactory

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        GradleCompat.init(project)
    }

    def "registerAggregatedUploadApkTask should not add any task if empty is given"() {
        given:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)
        def taskNames = project.tasks.toList().collect { it.name }

        when:
        dslBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask()

        then:
        taskNames == project.tasks.toList().collect { it.name }

        when:
        dslBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask([])

        then:
        taskNames == project.tasks.toList().collect { it.name }
    }

    def "registerAggregatedUploadApkTask should add a task which run given tasks only once"() {
        given:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)

        when:
        dslBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask("task1", "task2")

        and:
        def task = project.tasks.findByName("uploadDeployGate")

        then:
        task.dependsOn.flatten().collect {
            if (it.hasProperty("taskName")) {
                it.taskName
            } else if (it.hasProperty("name")) {
                it.name
            } else {
                it
            }
        } == ["task1", "task2"]
    }

    def "registerAggregatedUploadApkTask should modify the existing itself if called twice"() {
        given:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)

        when:
        dslBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask("task1", "task2")
        dslBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask("task3", "task4")

        and:
        def task = project.tasks.findByName("uploadDeployGate")

        then:
        task.dependsOn.flatten().collect {
            if (it.hasProperty("taskName")) {
                it.taskName
            } else if (it.hasProperty("name")) {
                it.name
            } else {
                it
            }
        } == ["task1", "task2", "task3", "task4"]
    }

    def "registerUploadApkTask should add a UploadApkTask"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)

        when:
        dslBasedUploadApkTaskFactory.registerUploadArtifactTask("dep1")

        and:
        def task = project.tasks.findByName("uploadDeployGateDep1")

        then:
        task
        task instanceof UploadApkTask
        task.group == DeployGateTaskFactory.GROUP_NAME
    }

    def "registerUploadApkTask should not override itself if already exist"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)

        and:
        project.tasks.create("uploadDeployGateDep1")

        when:
        dslBasedUploadApkTaskFactory.registerUploadArtifactTask("dep1")

        and:
        def task = project.tasks.findByName("uploadDeployGateDep1")

        then:
        task
        !(task instanceof UploadApkTask)
    }

    def "registerUploadApkTask should not allow adding names which do not exist in build.gradle"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadApkTaskFactory = new DSLBasedUploadApkTaskFactory(project)

        when:
        dslBasedUploadApkTaskFactory.registerUploadArtifactTask("dep2")

        then:
        thrown(GradleException)
    }
}
