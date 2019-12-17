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
    final <T extends Task> LazyConfigurableTask<T> register(@Nonnull String taskName, @Nonnull Class<T> klass) {
        def existingTask = findByName(taskName)

        if (existingTask) {
            return null
        }

        return GradleCompat.newLazyConfigurableTask(project, taskName, klass)
    }

    @Nullable
    final <T extends Task> LazyConfigurableTask<T> registerOrFindBy(@Nonnull String taskName, @Nonnull Class<T> klass) {
        def existingTask = findByName(taskName)

        if (existingTask) {
            return new SingleTask(existingTask as T)
        }

        return GradleCompat.newLazyConfigurableTask(project, taskName, klass)
    }

    final boolean exists(@Nonnull String taskName) {
        return findByName(taskName)
    }

    @Nullable
    private Task findByName(@Nonnull String taskName) {
        return project.tasks.findByName(taskName)
    }
}
