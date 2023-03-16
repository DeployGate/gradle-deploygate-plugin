package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull

class DefaultPresetApkInfo extends DirectApkInfo {
    DefaultPresetApkInfo(@NotNull String variantName) {
        super(variantName, null, true, true)
    }
}
