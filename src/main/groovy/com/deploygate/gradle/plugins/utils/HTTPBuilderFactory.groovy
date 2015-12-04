package com.deploygate.gradle.plugins.utils

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

    static HTTPBuilder httpBuilder(endpoint) {
        setDefaultProxy new HTTPBuilder(endpoint)
    }

    static RESTClient restClient(endpoint) {
        setDefaultProxy new RESTClient(endpoint)
    }
}
