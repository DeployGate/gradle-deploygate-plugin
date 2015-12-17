package com.deploygate.gradle.plugins.utils

class UrlUtils {
    static def toQueryString(Map<String, String> data) {
        data.collect { String key, String value ->
            URLEncoder.encode(key, 'UTF-8') + "=" + URLEncoder.encode(value, 'UTF-8')
        }.join("&")
    }

    static def parseQueryString(String s) {
        (s ?: "").split("&").inject([:]) { LinkedHashMap<String, String> m, String str ->
            def (k, v) = str.split("=", 2).collect { URLDecoder.decode(it, 'UTF-8') }
            m.put k, v
            m
        } as LinkedHashMap<String, String>
    }
}
