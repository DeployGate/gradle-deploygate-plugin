package com.deploygate.gradle.plugins.tasks.inputs;

import org.gradle.api.tasks.Input;

public abstract class Credentials {
    @Input
    public String appOwnerName;

    @Input
    public String apiToken;
}
