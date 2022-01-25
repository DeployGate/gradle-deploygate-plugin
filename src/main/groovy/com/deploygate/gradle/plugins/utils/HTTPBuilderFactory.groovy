package com.deploygate.gradle.plugins.utils

import com.deploygate.gradle.plugins.Config
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner

class HTTPBuilderFactory {
    static HTTPBuilder setDefaultProxy(HTTPBuilder httpBuilder) {
        httpBuilder.client.routePlanner = new ProxySelectorRoutePlanner(
                httpBuilder.client.connectionManager.schemeRegistry,
                ProxySelector.default
        )
        httpBuilder
    }

    static HTTPBuilder setDefaultRequestHeaders(HTTPBuilder httpBuilder) {
        httpBuilder.headers = [
                'User-Agent': "gradle-deploygate-plugin/${Config.VERSION}",
                'X-DEPLOYGATE-CLIENT-ID': "gradle-plugin/${Config.VERSION_CODE}",
                'X-DEPLOYGATE-CLIENT-VERSION-NAME': "${Config.VERSION}-${Config.VERSION_NAME}"
        ]
        httpBuilder
    }

    static HTTPBuilder httpBuilder(endpoint) {
        setDefaultProxy(setDefaultRequestHeaders(new HTTPBuilder(endpoint)))
    }

    static RESTClient restClient(endpoint) {
        setDefaultProxy(setDefaultRequestHeaders(new RESTClient(endpoint))) as RESTClient
    }
}
