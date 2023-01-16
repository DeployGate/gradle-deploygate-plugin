package com.deploygate.gradle.plugins.internal.http;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class ErrorResponse {

    @NotNull
    @SerializedName("message")
    public final String message = "";
}
