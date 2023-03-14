package com.deploygate.gradle.plugins.internal.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoOpLifecycleNotificationClient implements ILifecycleNotificationClient {
    @Override
    @Nullable public HttpClient.Response<GetCredentialsResponse> getCredentials() {
        return null;
    }

    @Override
    public boolean notifyOnCredentialSaved() {
        return false;
    }

    @Override
    public boolean notifyOnBeforeArtifactUpload(long fileSize) {
        return false;
    }

    @Override
    public boolean notifyOnSuccessOfArtifactUpload(@NotNull String appDetailPath) {
        return false;
    }

    @Override
    public boolean notifyOnFailureOfArtifactUpload(@NotNull String errorMessage) {
        return false;
    }
}
