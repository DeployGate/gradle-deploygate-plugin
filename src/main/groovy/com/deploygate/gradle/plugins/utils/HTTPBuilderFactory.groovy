package com.deploygate.gradle.plugins.utils

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.message.BasicHeader

class HTTPBuilderFactory {
    private static final Object LOCK = new Object()
    private static HttpClient httpClient

    static HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient
        }

        def headers = [
                'X-DEPLOYGATE-CLIENT-ID': "gradle-plugin/${Config.VERSION_CODE}",
                'X-DEPLOYGATE-CLIENT-VERSION-NAME': "${Config.VERSION}-${Config.VERSION_NAME}",
                'X-DEPLOYGATE-GRADLE-PLUGIN-AGP-VERSION': "${AndroidGradlePlugin.getVersion() ?: "null"}"
        ].collect {
            new BasicHeader(it.key, it.value)
        }

        synchronized (LOCK) {
            if (httpClient != null) {
                return httpClient
            }

            httpClient = HttpClientBuilder.create().
                    useSystemProperties().
                    setUserAgent("gradle-deploygate-plugin/${Config.VERSION}").
                    setDefaultHeaders(headers).
                    build()
        }

        return httpClient
    }
}
