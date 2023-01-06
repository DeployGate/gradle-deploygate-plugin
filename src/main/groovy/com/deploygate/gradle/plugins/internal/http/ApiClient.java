package com.deploygate.gradle.plugins.internal.http;

import com.deploygate.gradle.plugins.Config;
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin;
import com.deploygate.gradle.plugins.internal.annotation.Internal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {
    private static final Object LOCK = new Object();
    private static ApiClient sInstance;

    private static final Gson GSON = new GsonBuilder().create();

    private static String sEndpoint = Config.getDEPLOYGATE_ROOT();

    @Internal
    public static void setEndpoint(@Nullable String endpoint) {
        if (sInstance != null) {
            // too late
            return;
        }

        ApiClient.sEndpoint = endpoint != null ? endpoint : Config.getDEPLOYGATE_ROOT();
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

    private ApiClient(@NotNull String endpoint) {
        this.endpoint = endpoint;

        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader("X-DEPLOYGATE-CLIENT-ID", "gradle-plugin/" + Config.getVERSION_CODE()));
        headers.add(new BasicHeader("X-DEPLOYGATE-CLIENT-VERSION-NAME", Config.getVERSION() + "-" + Config.getVERSION_NAME()));
        headers.add(new BasicHeader("X-DEPLOYGATE-GRADLE-PLUGIN-AGP-VERSION", String.valueOf(AndroidGradlePlugin.getVersion())));

        this.httpClient = HttpClientBuilder.create().
                useSystemProperties().
                setUserAgent("gradle-deploygate-plugin/" + Config.getVERSION()).
                setDefaultHeaders(headers).
                build();
    }

    @SuppressWarnings("RedundantThrows")
    @NotNull
    public Response<UploadAppResponse> uploadApp(@NotNull String appOwnerName, @NotNull String apiToken, @NotNull UploadAppRequest request) throws HttpException, NetworkFailure {
        HttpPost httpPost = new HttpPost(endpoint + "/api/users/" + appOwnerName + "/apps");
        httpPost.setHeader("Authorization", "Bearer " + apiToken);
        httpPost.setEntity(request.toEntity());

        try {
            return httpClient.execute(httpPost, new ResponseSerializer<>(UploadAppResponse.class));
        } catch (IOException e) {
            throw new NetworkFailure("failed while uploading apps", e);
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
