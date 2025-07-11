package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.VersionString

class Config {
    static final String DEPLOYGATE_ROOT = 'https://deploygate.com'
    static final String VERSION
    static final String VERSION_CODE
    static final String VERSION_NAME

    static {
        VERSION = Config.class.getResourceAsStream("/VERSION").text.trim()
        def versionStr = VersionString.tryParse(VERSION)

        if (versionStr != null) {
            VERSION_CODE = versionStr.toLong()
        } else {
            VERSION_CODE = Integer.MAX_VALUE
        }


        def version_name_stream = Config.class.getResourceAsStream("/VERSION_NAME")

        if (version_name_stream != null) {
            VERSION_NAME = version_name_stream.text.trim()
        } else {
            VERSION_NAME = "unavailable"
        }
    }
}
