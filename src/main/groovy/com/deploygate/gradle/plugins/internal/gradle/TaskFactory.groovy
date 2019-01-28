package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.api.Task

import javax.annotation.Nonnull

class TaskFactory {
    @Nonnull
    private final Project project

    @Nonnull
    private final VersionString gradleVersion

    TaskFactory(@Nonnull Project project) {
        this.project = project
        this.gradleVersion = VersionString.tryParse(project.gradle.gradleVersion)
    }

    final <T extends Task> LazyConfigurableTask<T> register(String name, Class<T> klass) {
        if (gradleVersion.major >= 4 && gradleVersion.minor >= 8) {
            return new TaskProvider(project.tasks.register(name, klass))
        } else {
            return new SingleTask(project.tasks.create(name, klass))
        }
    }
}
