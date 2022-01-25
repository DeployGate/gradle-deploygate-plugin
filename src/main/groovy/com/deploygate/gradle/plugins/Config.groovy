package com.deploygate.gradle.plugins

class Config {
    static final def DEPLOYGATE_ROOT = 'https://deploygate.com'
    static final def VERSION
    static final def VERSION_NAME

    static {
        VERSION = Config.class.getResourceAsStream("/VERSION").text.trim()
        def version_name_stream = Config.class.getResourceAsStream("/VERSION_NAME")

        if (version_name_stream != null) {
            VERSION_NAME = version_name_stream.text.trim()
        } else {
            VERSION_NAME = "unavailable"
        }
    }

    static boolean shouldOpenAppDetailAfterUpload() {
        return System.getenv(DeployGatePlugin.ENV_NAME_OPEN_APP_DETAIL_AFTER_UPLOAD)
    }
}
