package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jetbrains.annotations.NotNull;

public class ApiClient {
    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final String apiToken;

    /**
     * @param httpClient should be immutable
     * @param apiToken   an authorization token
     */
    ApiClient(@NotNull HttpClient httpClient, @NotNull String apiToken) {
        this.httpClient = httpClient;
        this.apiToken = apiToken;
    }

    /**
     * Upload the application file to the app owner space
     *
     * @param appOwnerName an app owner name
     * @param request      a request to the server that must contain a file
     * @return a successful response that contains a typed json.
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure        is thrown if a network trouble happens
     */
    @SuppressWarnings("RedundantThrows")
    @NotNull
    public HttpClient.Response<UploadAppResponse> uploadApp(@NotNull String appOwnerName, @NotNull UploadAppRequest request) throws HttpResponseException, NetworkFailure {
        HttpUriRequestBase httpPost = httpClient.buildRequest(HttpPost.METHOD_NAME, "api", "users", appOwnerName, "apps");
        httpPost.setEntity(request.toEntity());

        configureAuthorizationHeader(httpPost);

        return httpClient.execute(httpPost, UploadAppResponse.class);
    }

    private void configureAuthorizationHeader(@NotNull HttpUriRequestBase base) {
        base.setHeader("Authorization", "Bearer " + apiToken);
    }
}
