package com.deploygate.gradle.plugins.artifacts

import javax.annotation.Nonnull

class DefaultPresetAabInfo extends DirectAabInfo {
    DefaultPresetAabInfo(@Nonnull String variantName) {
        super(variantName, null)
    }
}
