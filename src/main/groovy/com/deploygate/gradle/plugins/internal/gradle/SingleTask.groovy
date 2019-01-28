package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.Transformer
import org.gradle.api.internal.provider.TransformBackedProvider
import org.gradle.api.provider.Provider

class SingleTask<T extends Task> implements LazyConfigurableTask<T> {

    private final T task

    SingleTask(T task) {
        this.task = task
    }

    @Override
    void configure(Action<? super T> action) {
        // apply immediately
        action.execute(task)
    }

    @Override
    String getName() {
        return task.name
    }

    @Override
    T get() {
        if (!task) {
            throw new IllegalStateException("this must not be null")
        }

        return task
    }

    @Override
    T getOrNull() {
        return task
    }

    @Override
    T getOrElse(T defaultValue) {
        return task ?: defaultValue
    }

    @Override
    def <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
        return new TransformBackedProvider<S, T>(transformer, this)
    }

    @Override
    boolean isPresent() {
        return true
    }
}
