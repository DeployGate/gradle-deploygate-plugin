package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILifecycleNotificationClient {
    /**
     * Get the credentials from the server
     *
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure is thrown if a network trouble happens
     */
    @Nullable HttpClient.Response<GetCredentialsResponse> getCredentials()
            throws HttpResponseException, NetworkFailure;

    boolean notifyOnCredentialSaved();

    boolean notifyOnBeforeArtifactUpload(long fileSize);

    boolean notifyOnSuccessOfArtifactUpload(@NotNull String appDetailPath);

    boolean notifyOnFailureOfArtifactUpload(@NotNull String errorMessage);
}
