package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.internal.gradle.TaskFactory
import org.gradle.api.Project

import javax.annotation.Nonnull

abstract class DeployGateTaskFactory {
    public static final String GROUP_NAME = 'DeployGate'

    @Nonnull
    final Project project
    @Nonnull
    final TaskFactory taskFactory

    DeployGateTaskFactory(@Nonnull Project project) {
        this.project = project
        this.taskFactory = new TaskFactory(project)
    }

    private Closure<DeployGateExtension> deployGateExtensionClosure = { ->
        project.deploygate
    }.memoize()

    @Nonnull
    DeployGateExtension getDeployGateExtension() {
        return deployGateExtensionClosure.call()
    }
}
