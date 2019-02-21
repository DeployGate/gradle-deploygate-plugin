package com.deploygate.gradle.plugins.tasks.factory

interface LogoutTaskFactory {
    static String TASK_NAME = "logoutDeployGate"

    void registerLogoutTask()
}
