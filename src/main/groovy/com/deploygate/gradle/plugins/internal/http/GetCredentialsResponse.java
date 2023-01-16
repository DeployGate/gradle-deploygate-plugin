package com.deploygate.gradle.plugins.internal.http;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class GetCredentialsResponse {
    @NotNull
    @SerializedName("name")
    public final String name = "";

    @NotNull
    @SerializedName("token")
    public final String token = "";
}
