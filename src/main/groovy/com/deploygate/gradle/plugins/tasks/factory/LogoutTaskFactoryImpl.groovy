package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.tasks.LogoutTask
import org.gradle.api.Project

import javax.annotation.Nonnull

class LogoutTaskFactoryImpl extends DeployGateTaskFactory implements LogoutTaskFactory {
    LogoutTaskFactoryImpl(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerLogoutTask() {
        taskFactory.registerOrFindBy(TASK_NAME, LogoutTask).configure {
            it.group = GROUP_NAME
        }
    }
}
