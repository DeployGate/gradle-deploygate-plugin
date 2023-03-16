package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A client to notify lifecycle events to DeployGate server. */
public interface ILifecycleNotificationClient {
    /**
     * Get the credentials from the server
     *
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure is thrown if a network trouble happens
     */
    @Nullable HttpClient.Response<GetCredentialsResponse> getCredentials()
            throws HttpResponseException, NetworkFailure;

    /**
     * Notify the event after the fetched credentials are saved without any errors.
     *
     * @return true if notified, otherwise false.
     */
    boolean notifyOnCredentialSaved();

    /**
     * Notify the event before uploading apps.
     *
     * @return true if notified, otherwise false.
     */
    boolean notifyOnBeforeArtifactUpload(long fileSize);

    /**
     * Notify the vent after it succeeds to upload apps.
     *
     * @param appDetailPath relative path of the uploaded app's detail page
     * @return true if notified, otherwise false.
     */
    boolean notifyOnSuccessOfArtifactUpload(@NotNull String appDetailPath);

    /**
     * Notify the vent after it fails to upload apps.
     *
     * @param errorMessage an error message/reason
     * @return true if notified, otherwise false.
     */
    boolean notifyOnFailureOfArtifactUpload(@NotNull String errorMessage);
}
