package com.deploygate.gradle.plugins.artifacts

class DefaultPresetApkInfo extends DirectApkInfo {
    DefaultPresetApkInfo(String variantName) {
        super(variantName, null, true, true)
    }
}
