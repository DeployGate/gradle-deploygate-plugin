package com.deploygate.gradle.plugins.tasks.factory

interface LoginTaskFactory {
    static String TASK_NAME = "loginDeployGate"

    void registerLoginTask()
}
