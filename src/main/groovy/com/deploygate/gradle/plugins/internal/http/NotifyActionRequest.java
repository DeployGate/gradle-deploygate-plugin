package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class NotifyActionRequest {
    @NotNull
    private final String notifyKey;

    @NotNull
    private final String action;

    @NotNull
    private final Map<String, Object> data = new HashMap<>();

    public NotifyActionRequest(@NotNull String notifyKey, @NotNull String action) {
        this.notifyKey = Objects.requireNonNull(notifyKey);
        this.action = Objects.requireNonNull(action);
    }

    public void setParameter(@NotNull String name, @Nullable Object value) {
        data.put(name, value);
    }

    HttpEntity toEntity() {
        return new UrlEncodedFormEntity(buildNameValuePairs(), StandardCharsets.UTF_8);
    }

    @VisibleForTesting
    List<NameValuePair> buildNameValuePairs() {
        List<NameValuePair> values = new ArrayList<>();

        values.add(new BasicNameValuePair("key", notifyKey));
        values.add(new BasicNameValuePair("command_action", action));

        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (e.getValue() != null) {
                values.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
            } else {
                values.add(new BasicNameValuePair(e.getKey(), ""));
            }
        }

        return values;
    }
}
