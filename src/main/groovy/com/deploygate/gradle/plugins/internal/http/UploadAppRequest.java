package com.deploygate.gradle.plugins.internal.http;

import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class UploadAppRequest {
    @NotNull
    private final File appFile;

    @Nullable
    private String message;
    @Nullable
    private String distributionKey;
    @Nullable
    private String releaseNote;

    public UploadAppRequest(@NotNull File appFile) {
        this.appFile = appFile;
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

    @NotNull
    HttpEntity toEntity() {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setStrictMode();

        builder.addBinaryBody("file", appFile, ContentType.MULTIPART_FORM_DATA, appFile.getName());

        if (message != null) {
            builder.addPart("message", new StringBody(message, ContentType.MULTIPART_FORM_DATA));
        }

        if (distributionKey != null) {
            builder.addPart("distribution_key", new StringBody(distributionKey, ContentType.MULTIPART_FORM_DATA));

            if (releaseNote != null) {
                builder.addPart("release_note", new StringBody(releaseNote, ContentType.MULTIPART_FORM_DATA));
            }
        }

        return builder.build();
    }
}
