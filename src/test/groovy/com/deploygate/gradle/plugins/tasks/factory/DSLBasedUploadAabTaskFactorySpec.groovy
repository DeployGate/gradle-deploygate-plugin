package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.UploadAabTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class DSLBasedUploadAabTaskFactorySpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    @Nonnull
    private DSLBasedUploadAabTaskFactory dslBasedUploadAabTaskFactory

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        GradleCompat.init(project)
    }

    def "registerAggregatedUploadArtifactTask should not add any task if empty is given"() {
        given:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)
        def taskNames = project.tasks.toList().collect { it.name }

        when:
        dslBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask()

        then:
        taskNames == project.tasks.toList().collect { it.name }

        when:
        dslBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask([])

        then:
        taskNames == project.tasks.toList().collect { it.name }
    }

    def "registerAggregatedUploadArtifactTask should add a task which run given tasks only once"() {
        given:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)

        when:
        dslBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask("task1", "task2")

        and:
        def task = project.tasks.findByName("uploadDeployGateAab")

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

    def "registerAggregatedUploadArtifactTask should modify the existing itself if called twice"() {
        given:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)

        when:
        dslBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask("task1", "task2")
        dslBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask("task3", "task4")

        and:
        def task = project.tasks.findByName("uploadDeployGateAab")

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

    def "registerAggregatedUploadArtifactTask should add a UploadAabTask"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)

        when:
        dslBasedUploadAabTaskFactory.registerUploadArtifactTask("dep1")

        and:
        def task = project.tasks.findByName("uploadDeployGateAabDep1")

        then:
        task
        task instanceof UploadAabTask
        task.group == DeployGateTaskFactory.GROUP_NAME
    }

    def "registerAggregatedUploadArtifactTask should not override itself if already exist"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)

        and:
        project.tasks.create("uploadDeployGateAabDep1", UploadAabTask)

        when:
        dslBasedUploadAabTaskFactory.registerUploadArtifactTask("dep1")

        and:
        def task = project.tasks.findByName("uploadDeployGateAabDep1")

        then:
        task
    }

    def "registerAggregatedUploadArtifactTask should not allow adding names which do not exist in build.gradle"() {
        given:
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        deployments.create("dep1")
        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))

        and:
        dslBasedUploadAabTaskFactory = new DSLBasedUploadAabTaskFactory(project)

        when:
        dslBasedUploadAabTaskFactory.registerUploadArtifactTask("dep2")

        then:
        thrown(GradleException)
    }
}
