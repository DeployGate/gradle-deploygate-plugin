package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class LifecycleNotificationClient {
    @NotNull
    private final HttpClient httpClient;
    @NotNull
    private final String notifyKey;

    /**
     * @param httpClient
     * @param notifyKey  a notification key. In general, this is generated in the authentication flow.
     */
    LifecycleNotificationClient(@NotNull HttpClient httpClient, @NotNull String notifyKey) {
        this.httpClient = httpClient;
        this.notifyKey = notifyKey;
    }

    /**
     * Get the credentials from the server
     *
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure        is thrown if a network trouble happens
     */
    @NotNull
    public HttpClient.Response<GetCredentialsResponse> getCredentials() throws HttpResponseException, NetworkFailure {
        HttpUriRequestBase requestBase = httpClient.buildRequest(HttpGet.METHOD_NAME, Collections.singletonMap("key", notifyKey), "cli", "credential");

        return httpClient.execute(requestBase, GetCredentialsResponse.class);
    }

    /**
     * Notify the plugin event to the server
     *
     * @param request a request to the server that must contain an action name
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure        is thrown if a network trouble happens
     */
    @SuppressWarnings("RedundantThrows")
    public void notify(@NotNull NotifyActionRequest request) throws HttpResponseException, NetworkFailure {
        HttpUriRequestBase httpPost = httpClient.buildRequest(HttpPost.METHOD_NAME, "cli", "notify");
        httpPost.setEntity(request.toEntity(notifyKey));

        httpClient.execute(httpPost, Object.class);
    }
}
