package com.deploygate.gradle.plugins.tasks.inputs;

import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Locale;

public abstract class Credentials {
    @Input
    @Optional
    public abstract Property<String> getAppOwnerName();

    @Input
    @Optional
    public abstract Property<String> getApiToken();

    /**
     * Normalize and validate the properties. This throws an exception if invalid.
     *
     * @throws GradleException if invalid
     */
    @SuppressWarnings("UnstableApiUsage")
    public void normalizeAndValidate() {
        presence(getAppOwnerName(), "application owner name").finalizeValue();
        presence(getApiToken(), "api token").finalizeValue();
    }

    @NotNull
    @VisibleForTesting
    static Property<String> presence(@NotNull Property<String> property, @NotNull String displayName) {
        // To keep the backward compatibility, we have to trim values *safely*.
        final String value = (property.getOrNull() != null ? property.get() : "").trim();

        if (value.isEmpty()) {
            throw new GradleException(String.format(Locale.US, "%s is missing. Please configure the value properly.", displayName));
        } else {
            property.set(value);
        }

        return property;
    }
}
