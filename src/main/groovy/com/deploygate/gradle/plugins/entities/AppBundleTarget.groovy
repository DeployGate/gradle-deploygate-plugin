package com.deploygate.gradle.plugins.entities

import org.gradle.util.Configurable

interface AppBundleTarget extends Configurable<AppBundleTarget> {
    void setSource(String path)

    void setSkipBundle(boolean skipBundle)
}