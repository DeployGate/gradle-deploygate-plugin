package com.deploygate.gradle.plugins.internal.http;

import com.deploygate.gradle.plugins.tasks.inputs.Credentials;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jetbrains.annotations.NotNull;

public class ApiClient {
    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final Credentials credentials;

    /**
     * @param httpClient should be immutable
     * @param credentials a credential of those requests
     */
    ApiClient(@NotNull HttpClient httpClient, @NotNull Credentials credentials) {
        this.httpClient = httpClient;
        this.credentials = credentials;
    }

    /**
     * Upload the application file to the app owner space
     *
     * @param request      a request to the server that must contain a file
     * @return a successful response that contains a typed json.
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure        is thrown if a network trouble happens
     */
    @SuppressWarnings("RedundantThrows")
    @NotNull
    public HttpClient.Response<UploadAppResponse> uploadApp(@NotNull UploadAppRequest request) throws HttpResponseException, NetworkFailure {
        HttpUriRequestBase httpPost = httpClient.buildRequest(HttpPost.METHOD_NAME, "api", "users", credentials.getAppOwnerName().get(), "apps");
        httpPost.setEntity(request.toEntity());

        configureAuthorizationHeader(httpPost);

        return httpClient.execute(httpPost, UploadAppResponse.class);
    }

    private void configureAuthorizationHeader(@NotNull HttpUriRequestBase base) {
        base.setHeader("Authorization", "Bearer " + credentials.getApiToken().get());
    }
}
