package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.tasks.LoginTask
import org.gradle.api.Project

import javax.annotation.Nonnull

class LoginTaskFactoryImpl extends DeployGateTaskFactory implements LoginTaskFactory {
    LoginTaskFactoryImpl(@Nonnull Project project) {
        super(project)
    }

    @Override
    void registerLoginTask() {
        taskFactory.register(TASK_NAME, LoginTask).configure {
            it.group = GROUP_NAME
        }
    }
}
