package com.deploygate.gradle.plugins.tasks.factory


import com.deploygate.gradle.plugins.tasks.LoginTask
import org.gradle.api.Project

import javax.annotation.Nonnull

class LoginTaskFactory extends DeployGateTaskFactory {
    static String TASK_NAME = "loginDeployGate"

    LoginTaskFactory(@Nonnull Project project) {
        super(project)
    }

    void registerLoginTask() {
        taskFactory.register(TASK_NAME, LoginTask).configure {
            it.group = GROUP_NAME
        }
    }
}
