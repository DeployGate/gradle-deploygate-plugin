package com.deploygate.gradle.plugins.factory


import com.deploygate.gradle.plugins.tasks.LogoutTask
import org.gradle.api.Project

import javax.annotation.Nonnull

class LogoutTaskFactory extends DeployGateTaskFactory {
    static String TASK_NAME = "logoutDeployGate"

    LogoutTaskFactory(@Nonnull Project project) {
        super(project)
    }

    void registerLogoutTask() {
        taskFactory.register(TASK_NAME, LogoutTask).configure {
            it.group = GROUP_NAME
        }
    }
}
