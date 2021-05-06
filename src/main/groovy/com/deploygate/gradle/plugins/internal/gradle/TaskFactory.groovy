package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider

import javax.annotation.Nonnull
import javax.annotation.Nullable

class TaskFactory {
    @Nonnull
    private final Project project

    TaskFactory(@Nonnull Project project) {
        this.project = project
    }

    @Nullable
    final <T extends Task> TaskProvider<T> register(@Nonnull String taskName, @Nonnull Class<T> klass) {
        try {
            project.tasks.named(taskName, klass)
            return null
        } catch (UnknownTaskException ignore) {
            return project.tasks.register(taskName, klass)
        }
    }

    @Nullable
    final <T extends Task> TaskProvider<T> registerOrFindBy(@Nonnull String taskName, @Nonnull Class<T> klass) {
        try {
            return project.tasks.named(taskName, klass)
        } catch (UnknownTaskException ignore) {
            return project.tasks.register(taskName, klass)
        }
    }

    final boolean exists(@Nonnull String taskName) {
        return findByName(taskName)
    }

    @Nullable
    private Task findByName(@Nonnull String taskName) {
        return project.tasks.findByName(taskName)
    }
}
