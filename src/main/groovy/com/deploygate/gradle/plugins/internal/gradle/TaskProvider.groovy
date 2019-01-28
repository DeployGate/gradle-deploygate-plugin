package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.Transformer
import org.gradle.api.provider.Provider

import javax.annotation.Nonnull

/**
 * This is just a delegation of TaskProvider which has been introduced since Gradle 4.0
 * @param < T >
 */
class TaskProvider<T extends Task> implements LazyConfigurableTask<T> {

    @Nonnull
    private final org.gradle.api.tasks.TaskProvider<T> gradleTaskProvider

    TaskProvider(@Nonnull org.gradle.api.tasks.TaskProvider<T> gradleTaskProvider) {
        this.gradleTaskProvider = gradleTaskProvider
    }

    @Override
    void configure(Action<? super T> action) {
        gradleTaskProvider.configure(action)
    }

    @Override
    String getName() {
        return gradleTaskProvider.name
    }

    @Override
    T get() {
        return gradleTaskProvider.get()
    }

    @Override
    T getOrNull() {
        return gradleTaskProvider.getOrNull()
    }

    @Override
    T getOrElse(T defaultValue) {
        return gradleTaskProvider.getOrElse(defaultValue)
    }

    @Override
    def <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
        return gradleTaskProvider.map(transformer)
    }

    @Override
    boolean isPresent() {
        return gradleTaskProvider.isPresent()
    }
}
