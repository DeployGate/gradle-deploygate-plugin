package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.Project
import org.gradle.api.Task

import javax.annotation.Nonnull
import javax.annotation.Nullable

class TaskFactory {
    @Nonnull
    private final Project project

    TaskFactory(@Nonnull Project project) {
        this.project = project
    }

    @Nullable
    final <T extends Task> LazyConfigurableTask<T> register(@Nonnull String taskName, @Nonnull Class<T> klass, boolean allowExisting = true) {
        def existingTask = project.tasks.findByName(taskName)

        if (existingTask) {
            if (allowExisting) {
                return new SingleTask(existingTask as T)
            } else {
                return null
            }
        }

        return GradleCompat.newLazyConfigurableTask(project, taskName, klass)
    }
}
