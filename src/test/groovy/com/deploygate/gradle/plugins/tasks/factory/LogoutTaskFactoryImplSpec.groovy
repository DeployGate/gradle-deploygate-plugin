package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.LogoutTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.annotation.Nonnull

class LogoutTaskFactoryImplSpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private LogoutTaskFactoryImpl logoutTaskFactory

    def setup() {
        project = ProjectBuilder.builder().build()
        GradleCompat.init(project)
    }

    def "registerLogoutTask should add a LogoutTask"() {
        given:
        logoutTaskFactory = new LogoutTaskFactoryImpl(project)

        when:
        logoutTaskFactory.registerLogoutTask()

        and:
        def task = project.tasks.findByName("logoutDeployGate")

        then:
        task
        task instanceof LogoutTask
        task.group == DeployGateTaskFactory.GROUP_NAME
    }

    def "registerLogoutTask should add a LogoutTask only once"() {
        given:
        logoutTaskFactory = new LogoutTaskFactoryImpl(project)

        when:
        logoutTaskFactory.registerLogoutTask()

        and:
        def firstTask = project.tasks.findByName("logoutDeployGate")
        firstTask.description = "dummy description"

        then:
        firstTask.description == "dummy description"

        when:
        logoutTaskFactory.registerLogoutTask()

        and:
        def secondTask = project.tasks.findByName("logoutDeployGate")

        then:
        firstTask == secondTask
    }
}
