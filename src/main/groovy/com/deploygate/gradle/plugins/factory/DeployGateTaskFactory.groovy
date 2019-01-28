package com.deploygate.gradle.plugins.factory

import com.android.build.gradle.AndroidConfig
import com.deploygate.gradle.plugins.entities.DeployGateExtension
import com.deploygate.gradle.plugins.internal.gradle.TaskFactory
import org.gradle.api.Project

import javax.annotation.Nonnull

abstract class DeployGateTaskFactory {
    static final String GROUP_NAME = 'DeployGate'

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

    private AndroidConfig _androidConfig

    @Nonnull
    AndroidConfig getAndroidExtension() {
        if (_androidConfig) {
            return _androidConfig
        }

        _androidConfig = project.android

        return _androidConfig
    }
}
