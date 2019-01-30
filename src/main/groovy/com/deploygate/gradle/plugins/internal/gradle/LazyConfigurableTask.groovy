package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Provider

interface LazyConfigurableTask<T extends Task> extends Provider<T> {
    /**
     * Configures the task with the given action. Actions are run in the order added.
     *
     * @param action A {@link org.gradle.api.Action} that can configure the task when required.
     */
    void configure(Action<? super T> action);

    /**
     * The task name referenced by this provider.
     * <p>
     * Must be constant for the life of the object.
     *
     * @return The task name. Never null.
     */
    String getName();
}
