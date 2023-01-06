package com.deploygate.gradle.plugins.internal.http;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class UploadAppResponse {
    @SuppressWarnings("ConstantConditions")
    @NotNull
    @SerializedName("results")
    public final ApplicationFragment application = null;

    static class ApplicationFragment {
        @NotNull
        @SerializedName("path")
        public final String path = "";

        @SerializedName("revision")
        public final long revision = 0;
    }
}
