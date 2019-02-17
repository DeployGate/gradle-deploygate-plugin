package com.deploygate.gradle.plugins.utils

class UrlUtils {
    static def parseQueryString(String s) {
        (s ?: "").split("&").inject([:]) { LinkedHashMap<String, String> m, String str ->
            def (k, v) = str.split("=", 2).collect { URLDecoder.decode(it, 'UTF-8') }
            m.put k, v
            m
        } as LinkedHashMap<String, String>
    }
}
