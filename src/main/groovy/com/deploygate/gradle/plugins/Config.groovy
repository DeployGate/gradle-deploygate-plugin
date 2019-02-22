package com.deploygate.gradle.plugins

class Config {
    static final def VERSION = Config.class.getResourceAsStream("/VERSION").text.trim()
    static final def USER_AGENT = "gradle-deploygate-plugin/${VERSION}"
    static final def DEPLOYGATE_ROOT = 'https://deploygate.com'

    static boolean shouldOpenAppDetailAfterUpload() {
        return System.getenv(DeployGatePlugin.ENV_NAME_OPEN_APP_DETAIL_AFTER_UPLOAD)
    }
}
