package com.deploygate.gradle.plugins.internal.http;

import java.io.IOException;

public class NetworkFailure extends IOException {
    public NetworkFailure(String message, IOException cause) {
        super(message, cause);
    }
}
