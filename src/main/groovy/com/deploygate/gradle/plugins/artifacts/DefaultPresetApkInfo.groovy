package com.deploygate.gradle.plugins.artifacts

import javax.annotation.Nonnull

class DefaultPresetApkInfo extends DirectApkInfo {
    DefaultPresetApkInfo(@Nonnull String variantName) {
        super(variantName, null, true, true)
    }
}
