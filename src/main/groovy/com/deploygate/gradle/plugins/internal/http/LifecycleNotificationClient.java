package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class LifecycleNotificationClient implements ILifecycleNotificationClient {
    @NotNull
    private final HttpClient httpClient;
    @NotNull
    private final String notifyKey;

    /**
     * @param httpClient a real http client
     * @param notifyKey  a notification key. In general, this is generated in the authentication flow.
     */
    LifecycleNotificationClient(@NotNull HttpClient httpClient, @NotNull String notifyKey) {
        this.httpClient = httpClient;
        this.notifyKey = notifyKey;
    }

    @NotNull
    @Override
    public HttpClient.Response<GetCredentialsResponse> getCredentials() throws HttpResponseException, NetworkFailure {
        HttpUriRequestBase requestBase = httpClient.buildRequest(HttpGet.METHOD_NAME, Collections.singletonMap("key", notifyKey), "cli", "credential");

        return httpClient.execute(requestBase, GetCredentialsResponse.class);
    }

    @Override
    public boolean notifyOnCredentialSaved() {
        return notify(new NotifyActionRequest("credential_saved"));
    }

    @Override
    public boolean notifyOnBeforeArtifactUpload(long fileSize) {
        NotifyActionRequest request = new NotifyActionRequest("start_upload");
        request.setParameter("length", Long.toString(fileSize));
        return notify(request);
    }

    @Override
    public boolean notifyOnSuccessOfArtifactUpload(@NotNull String appDetailPath) {
        NotifyActionRequest request = new NotifyActionRequest("'upload_finished'");
        request.setParameter("path", appDetailPath);
        return notify(request);
    }

    @Override
    public boolean notifyOnFailureOfArtifactUpload(@NotNull String errorMessage) {
        NotifyActionRequest request = new NotifyActionRequest("'upload_finished'");
        request.setParameter("error", "true");
        request.setParameter("message", errorMessage);
        return notify(request);
    }

    /**
     * Notify the plugin event to the server
     *
     * @param request a request to the server that must contain an action name
     * @return true anyway
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure        is thrown if a network trouble happens
     */
    private boolean notify(@NotNull NotifyActionRequest request) {
        try {
            HttpUriRequestBase httpPost = httpClient.buildRequest(HttpPost.METHOD_NAME, "cli", "notify");
            httpPost.setEntity(request.toEntity(notifyKey));

            httpClient.execute(httpPost, Object.class);
            return true;
        } catch (Throwable ignore) {
            return true; // returns true anyway!
        }
    }
}
