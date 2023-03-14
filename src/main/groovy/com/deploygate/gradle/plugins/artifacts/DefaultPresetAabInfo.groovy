package com.deploygate.gradle.plugins.artifacts

import org.jetbrains.annotations.NotNull

class DefaultPresetAabInfo extends DirectAabInfo {
    DefaultPresetAabInfo(@NotNull String variantName) {
        super(variantName, null)
    }
}
