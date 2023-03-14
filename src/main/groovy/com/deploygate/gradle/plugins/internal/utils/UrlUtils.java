package com.deploygate.gradle.plugins.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UrlUtils {
    private UrlUtils() {
    }

    @NotNull
    public static Map<String, String> parseQueryString(@NotNull String s) {
        if (StringUtils.isNullOrEmpty(s)) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();

        for (final String part : s.split("&")) {
            if (part.isEmpty()) {
                continue;
            }

            String[] kv = part.split("=", 2);

            try {
                params.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            } catch (UnsupportedEncodingException never) {
                throw new RuntimeException(never);
            }
        }

        return params;
    }
}
