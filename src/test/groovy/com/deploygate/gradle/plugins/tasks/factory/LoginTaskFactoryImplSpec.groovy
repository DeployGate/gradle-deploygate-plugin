package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.tasks.LoginTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.annotation.Nonnull

class LoginTaskFactoryImplSpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private LoginTaskFactoryImpl loginTaskFactory

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "registerLoginTask should add a LoginTask"() {
        given:
        loginTaskFactory = new LoginTaskFactoryImpl(project)

        when:
        loginTaskFactory.registerLoginTask()

        and:
        def task = project.tasks.findByName(LoginTaskFactory.TASK_NAME)

        then:
        task
        task instanceof LoginTask
        task.group == DeployGateTaskFactory.GROUP_NAME
    }

    def "registerLoginTask should add a LoginTask only once"() {
        given:
        loginTaskFactory = new LoginTaskFactoryImpl(project)

        when:
        loginTaskFactory.registerLoginTask()

        and:
        def firstTask = project.tasks.findByName(LoginTaskFactory.TASK_NAME)
        firstTask.description = "dummy description"

        then:
        firstTask.description == "dummy description"

        when:
        loginTaskFactory.registerLoginTask()

        and:
        def secondTask = project.tasks.findByName(LoginTaskFactory.TASK_NAME)

        then:
        firstTask == secondTask
    }
}
