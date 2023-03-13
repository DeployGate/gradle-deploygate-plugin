package com.deploygate.gradle.plugins.internal.http;

import com.deploygate.gradle.plugins.Config;
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin;
import com.deploygate.gradle.plugins.internal.annotation.DeployGateInternal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final Object LOCK = new Object();
    private static ApiClient sInstance;

    @VisibleForTesting
    static final Gson GSON = new GsonBuilder().create();

    private static String sEndpoint = Config.getDEPLOYGATE_ROOT();

    @DeployGateInternal
    public static void setEndpoint(@Nullable String endpoint) {
        if (sInstance != null) {
            // too late
            return;
        }

        ApiClient.sEndpoint = endpoint != null ? endpoint : Config.getDEPLOYGATE_ROOT();
    }

    @NotNull
    @DeployGateInternal
    @Deprecated
    public static String getEndpoint() {
        return sEndpoint;
    }

    @VisibleForTesting
    static void clear() {
        sEndpoint = Config.getDEPLOYGATE_ROOT();
        sInstance = null;
    }

    @NotNull
    public static ApiClient getInstance() {
        if (sInstance != null) {
            return sInstance;
        }

        synchronized (LOCK) {
            if (sInstance != null) {
                return sInstance;
            }

            sInstance = new ApiClient(sEndpoint);
        }

        return sInstance;
    }

    @NotNull
    private final HttpClient httpClient;
    @NotNull
    private final String endpoint;

    @VisibleForTesting
    ApiClient(@NotNull String endpoint) {
        this.endpoint = endpoint;

        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader("X-DEPLOYGATE-CLIENT-ID", "gradle-plugin/" + Config.getVERSION_CODE()));
        headers.add(new BasicHeader("X-DEPLOYGATE-CLIENT-VERSION-NAME", Config.getVERSION() + "-" + Config.getVERSION_NAME()));
        headers.add(new BasicHeader("X-DEPLOYGATE-GRADLE-PLUGIN-AGP-VERSION", String.valueOf(AndroidGradlePlugin.getVersion())));

        RequestConfig requestConfig = RequestConfig.custom().
                setExpectContinueEnabled(true).
                setMaxRedirects(3).
                setConnectionRequestTimeout(10, TimeUnit.MINUTES).
                setDefaultKeepAlive(10, TimeUnit.MINUTES).
                setResponseTimeout(10, TimeUnit.MINUTES).
                build();

        this.httpClient = HttpClientBuilder.create().
                useSystemProperties().
                setUserAgent("gradle-deploygate-plugin/" + Config.getVERSION()).
                setDefaultHeaders(headers).
                setDefaultRequestConfig(requestConfig).
                build();
    }

    /**
     * Upload the application file to the app owner space
     *
     * @param appOwnerName an app owner name
     * @param apiToken an authorization token
     * @param request a request to the server that must contain a file
     * @return a successful response that contains a typed json.
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure is thrown if a network trouble happens
     */
    @SuppressWarnings("RedundantThrows")
    @NotNull
    public Response<UploadAppResponse> uploadApp(@NotNull String appOwnerName, @NotNull String apiToken, @NotNull UploadAppRequest request) throws HttpResponseException, NetworkFailure {
        HttpPost httpPost = new HttpPost(endpoint + "/api/users/" + appOwnerName + "/apps");
        httpPost.setHeader("Authorization", "Bearer " + apiToken);
        httpPost.setEntity(request.toEntity());

        try {
            return httpClient.execute(httpPost, new ResponseSerializer<>(UploadAppResponse.class));
        } catch (HttpResponseException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkFailure("internet connection had trouble while uploading apps", e);
        }
    }

    /**
     * Notify the plugin event to the server
     *
     * @param request a request to the server that must contain an action name
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure is thrown if a network trouble happens
     */
    @SuppressWarnings("RedundantThrows")
    public void notify(@NotNull NotifyActionRequest request) throws HttpResponseException, NetworkFailure {
        HttpPost httpPost = new HttpPost(endpoint + "/cli/notify");
        httpPost.setEntity(request.toEntity());

        try {
            httpClient.execute(httpPost, new ResponseSerializer<>(Object.class));
        } catch (HttpResponseException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkFailure("internet connection had trouble while notifying the action to the server", e);
        }
    }

    /**
     * Get the credentials from the server
     *
     * @param notifyKey a notification key. In general, this is generated in the authentication flow.
     * @throws HttpResponseException is thrown if a request is an error inclduing 4xx and 5xx
     * @throws NetworkFailure is thrown if a network trouble happens
     */
    @NotNull
    public Response<GetCredentialsResponse> getCredentials(@NotNull String notifyKey) throws HttpResponseException, NetworkFailure {
        URI uri;

        try {
            uri = new URIBuilder(endpoint + "/cli/credential")
                    .addParameter("key", notifyKey)
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpGet httpGet = new HttpGet(uri);

        try {
            return httpClient.execute(httpGet, new ResponseSerializer<>(GetCredentialsResponse.class));
        } catch (HttpResponseException e) {
            throw e;
        } catch (IOException e) {
            throw new NetworkFailure("internet connection had trouble while notifying the action to the server", e);
        }
    }

    public static class Response<T> {
        @NotNull
        public final T typedResponse;
        @NotNull
        public final String rawResponse;

        public Response(@NotNull T typedResponse, @NotNull String rawResponse) {
            this.typedResponse = typedResponse;
            this.rawResponse = rawResponse;
        }
    }

    private static class ResponseSerializer<T> extends AbstractHttpClientResponseHandler<Response<T>> {
        private final Class<T> klass;

        ResponseSerializer(Class<T> klass) {
            this.klass = klass;
        }

        @Override
        public Response<T> handleResponse(ClassicHttpResponse response) throws IOException {
            if (400 <= response.getCode() && response.getCode() < 500) {
                HttpEntity entity = response.getEntity();
                String responseString = stringify(entity.getContent());

                try {
                    ErrorResponse error = GSON.fromJson(responseString, ErrorResponse.class);
                    response.setReasonPhrase(error.message);
                } catch (JsonIOException | JsonSyntaxException ignore) {
                }
            }

            return super.handleResponse(response);
        }

        @Override
        public Response<T> handleEntity(HttpEntity entity) throws IOException {
            String responseString = stringify(entity.getContent());

            return new Response<>(
                    GSON.fromJson(responseString, klass),
                    responseString
            );
        }

        private static String stringify(@NotNull InputStream is) throws IOException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();

                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
            }
        }
    }
}
