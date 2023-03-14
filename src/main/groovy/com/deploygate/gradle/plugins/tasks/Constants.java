package com.deploygate.gradle.plugins.tasks;

import javax.annotation.Nonnull;
import java.util.Locale;

public class Constants {
    public static final String TASK_GROUP_NAME = "DeployGate";

    public static final String LOGIN_TASK_NAME = "loginDeployGate";
    public static final String LOGOUT_TASK_NAME = "logoutDeployGate";

    public static String SUFFIX_APK_TASK_NAME = "uploadDeployGate";
    public static String SUFFIX_AAB_TASK_NAME = "uploadDeployGateAab";

    @Nonnull
    public static String uploadApkTaskName(@Nonnull String variantName) {
        return SUFFIX_APK_TASK_NAME + capitalize(variantName);
    }

    @Nonnull
    public static String uploadAabTaskName(@Nonnull String variantName) {
        return SUFFIX_AAB_TASK_NAME + capitalize(variantName);
    }

    @Nonnull
    private static String capitalize(@Nonnull String value) {
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }
}
