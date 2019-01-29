package com.deploygate.gradle.plugins.factory


import com.deploygate.gradle.plugins.entities.DeployGateExtension
import com.deploygate.gradle.plugins.internal.gradle.TaskFactory
import org.gradle.api.Project

import javax.annotation.Nonnull

abstract class DeployGateTaskFactory {
    public static final String GROUP_NAME = 'DeployGate'

    @Nonnull
    final Project project
    final TaskFactory taskFactory

    DeployGateTaskFactory(@Nonnull Project project) {
        this.project = project
        this.taskFactory = new TaskFactory(project)
    }

    private DeployGateExtension _deployGateExtension

    @Nonnull
    DeployGateExtension getDeployGateExtension() {
        if (_deployGateExtension) {
            return _deployGateExtension
        }

        _deployGateExtension = project.deploygate

        return _deployGateExtension
    }
}
