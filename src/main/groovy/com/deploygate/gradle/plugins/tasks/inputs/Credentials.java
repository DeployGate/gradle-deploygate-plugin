package com.deploygate.gradle.plugins.tasks.inputs;

import static com.deploygate.gradle.plugins.internal.gradle.PropertyUtils.presence;

import java.util.Locale;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;

public abstract class Credentials {
    @Input
    @Optional
    public abstract Property<String> getAppOwnerName();

    @Input
    @Optional
    public abstract Property<String> getApiToken();

    @Internal
    public boolean isPresent() {
        return getAppOwnerName().isPresent() && getApiToken().isPresent();
    }

    public void normalize() {
        presence(getAppOwnerName());
        presence(getApiToken());
    }

    /**
     * Normalize and validate the properties. This throws an exception if invalid.
     *
     * @throws GradleException if invalid
     */
    @SuppressWarnings("UnstableApiUsage")
    public void normalizeAndValidate() {
        normalize();

        if (!getAppOwnerName().isPresent()) {
            throw new GradleException(
                    String.format(
                            Locale.US,
                            "%s is missing. Please configure the value properly.",
                            "application owner name"));
        }

        if (!getApiToken().isPresent()) {
            throw new GradleException(
                    String.format(
                            Locale.US,
                            "%s is missing. Please configure the value properly.",
                            "api token"));
        }

        getAppOwnerName().finalizeValue();
        getApiToken().finalizeValue();
    }
}
