package com.deploygate.gradle.plugins.internal.http;

import static org.apache.hc.client5.http.entity.mime.HttpMultipartMode.EXTENDED;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UploadAppRequest {
    private static final ContentType UTF8_PLAIN_TEXT =
            ContentType.create("text/plain", StandardCharsets.UTF_8);

    @NotNull private final File appFile;

    @Nullable private String message;
    @Nullable private String distributionKey;
    @Nullable private String releaseNote;

    @Nullable private MultipartEntityBuilder builder;

    public UploadAppRequest(@NotNull File appFile) {
        this(appFile, null);
    }

    UploadAppRequest(@NotNull File appFile, @Nullable MultipartEntityBuilder builder) {
        this.appFile = appFile;
        this.builder = builder;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    public void setDistributionKey(@Nullable String distributionKey) {
        this.distributionKey = distributionKey;
    }

    public void setReleaseNote(@Nullable String releaseNote) {
        this.releaseNote = releaseNote;
    }

    @NotNull HttpEntity toEntity() {
        MultipartEntityBuilder builder;

        if (this.builder != null) {
            builder = this.builder;
        } else {
            builder = MultipartEntityBuilder.create();
        }

        builder.setMode(EXTENDED);
        builder.setCharset(StandardCharsets.UTF_8);
        builder.addBinaryBody("file", appFile);

        if (message != null) {
            builder.addPart("message", new StringBody(message, UTF8_PLAIN_TEXT));
        }

        if (distributionKey != null) {
            builder.addPart("distribution_key", new StringBody(distributionKey, UTF8_PLAIN_TEXT));

            if (releaseNote != null) {
                builder.addPart("release_note", new StringBody(releaseNote, UTF8_PLAIN_TEXT));
            }
        }

        return builder.build();
    }
}
