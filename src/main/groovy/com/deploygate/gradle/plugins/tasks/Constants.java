package com.deploygate.gradle.plugins.tasks;

import com.deploygate.gradle.plugins.internal.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Constants {
    public static final String TASK_GROUP_NAME = "DeployGate";

    public static final String LOGIN_TASK_NAME = "loginDeployGate";
    public static final String LOGOUT_TASK_NAME = "logoutDeployGate";

    public static String SUFFIX_APK_TASK_NAME = "uploadDeployGate";
    public static String SUFFIX_AAB_TASK_NAME = "uploadDeployGateAab";

    @NotNull public static String uploadApkTaskName(@NotNull String variantName) {
        return SUFFIX_APK_TASK_NAME + StringUtils.capitalize(variantName);
    }

    @NotNull public static String uploadAabTaskName(@NotNull String variantName) {
        return SUFFIX_AAB_TASK_NAME + StringUtils.capitalize(variantName);
    }
}
