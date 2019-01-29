package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.api.Task

import javax.annotation.Nonnull
import javax.annotation.Nullable

class TaskFactory {
    @Nonnull
    private final Project project

    @Nonnull
    private final VersionString gradleVersion

    TaskFactory(@Nonnull Project project) {
        this.project = project
        this.gradleVersion = VersionString.tryParse(project.gradle.gradleVersion)
    }

    @Nullable
    final <T extends Task> LazyConfigurableTask<T> register(String taskName, Class<T> klass, boolean allowExisting = true) {
        def existingTask = project.tasks.findByName(taskName)

        if (existingTask) {
            if (allowExisting) {
                return new SingleTask(existingTask as T)
            } else {
                return null
            }
        }

        if (gradleVersion.major >= 4 && gradleVersion.minor >= 8) {
            return new TaskProvider(project.tasks.register(taskName, klass))
        } else {
            return new SingleTask(project.tasks.create(taskName, klass))
        }
    }
}
